package edu.hitsz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import edu.hitsz.aircraft.*;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.dao.CoinDAO;
import edu.hitsz.effect.ParticleSystem;
import edu.hitsz.factory.*;
import edu.hitsz.prop.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private int screenWidth, screenHeight;
    private Rect dstRect = new Rect();

    private String difficulty;
    private boolean musicOn = true;
    private int score = 0;
    private volatile boolean gameOver = false;
    private volatile boolean isRunning = false;
    private Thread gameThread;

    private HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts = new ArrayList<>();
    private final List<BaseBullet> heroBullets = new ArrayList<>();
    private final List<BaseBullet> enemyBullets = new ArrayList<>();
    private final List<AbstractProp> props = new ArrayList<>();

    private int enemyMaxNumber;
    private int bossThreshold;
    private int cycleDuration;
    private int heroShootInterval;
    private int baseHeroShootInterval;
    private boolean bossOnScreen = false;

    private long lastEnemyCycle = 0;
    private long lastHeroShoot = 0;
    private long lastDifficultyIncrease = 0;
    private long currentTime = 0;

    private int backGroundTop = 0;
    private Bitmap backgroundImage;

    private ScheduledExecutorService executorService;
    private Paint paint;
    private BombProp currentBomb = null;

    private volatile boolean isPaused = false;
    private final Object pauseLock = new Object();

    private AudioManager audioManager;

    private static final int MAX_HERO_BULLETS = 50;
    private static final int MAX_ENEMY_BULLETS = 120;
    private static final int COLLISION_DISTANCE = 120;

    private float superEliteSpeedFactor = 1.0f;
    private static final float MAX_SUPER_ELITE_FACTOR = 2.0f;

    private volatile boolean gameOverNotified = false;

    // ── 联机相关 ──────────────────────────────────────────────────────
    private boolean isMultiplayer = false;
    private String serverIp = "10.0.2.2";
    private static final int BATTLE_PORT = 9999;

    private volatile int enemyScore = 0;
    private volatile boolean enemyGameOver = false;
    private volatile boolean localGameOver = false;

    // 收到 action:start 前，只显示等待画面，不执行游戏逻辑
    private volatile boolean waitingForOpponent = false;

    private java.net.Socket socket;
    private java.io.PrintWriter writer;
    private java.io.BufferedReader reader;
    private int lastSentScore = -1;

    // ── Buff 系统 ─────────────────────────────────────────────────────
    private CoinDAO coinDAO;
    private boolean hasShield = false;
    private boolean hasDoubleGun = false;
    private long doubleGunEndTime = 0;
    private static final long DOUBLE_GUN_DURATION = 15_000L;
    private int coins = 0;

    // ── 粒子系统 ──────────────────────────────────────────────────────
    private final ParticleSystem particleSystem = new ParticleSystem();

    public GameView(Context context, String difficulty, boolean musicOn) {
        super(context);
        this.difficulty = difficulty;
        this.musicOn = musicOn;

        audioManager = AudioManager.getInstance(context);
        audioManager.setMusicOn(musicOn);
        audioManager.setSoundOn(musicOn);

        initView();
        initGame();
    }

    public void setMultiplayer(boolean multiplayer, String serverIp) {
        this.isMultiplayer = multiplayer;

        if (serverIp != null && !serverIp.isEmpty()) {
            this.serverIp = serverIp;
        }

        System.out.println("[联机] setMultiplayer: isMultiplayer="
                + isMultiplayer + ", serverIp=" + this.serverIp);
    }

    private void initView() {
        getHolder().addCallback(this);
        setFocusable(true);
        setKeepScreenOn(true);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(40);
    }

    private void initGame() {
        switch (difficulty) {
            case "easy":
                enemyMaxNumber = 3;
                bossThreshold = Integer.MAX_VALUE;
                cycleDuration = 1200;
                heroShootInterval = 300;
                backgroundImage = ImageManager.BACKGROUND_IMAGE;
                break;

            case "normal":
                enemyMaxNumber = 5;
                bossThreshold = 2000;
                cycleDuration = 1000;
                heroShootInterval = 400;
                backgroundImage = ImageManager.BACKGROUND_IMAGE3;
                break;

            case "hard":
                enemyMaxNumber = 7;
                bossThreshold = 2000;
                cycleDuration = 800;
                heroShootInterval = 300;
                backgroundImage = ImageManager.BACKGROUND_IMAGE5;
                break;

            default:
                enemyMaxNumber = 5;
                bossThreshold = 2000;
                cycleDuration = 600;
                heroShootInterval = 300;
                backgroundImage = ImageManager.BACKGROUND_IMAGE;
                break;
        }

        baseHeroShootInterval = heroShootInterval;

        HeroAircraft.resetInstance();
        heroAircraft = HeroAircraft.getInstance(0, 0, 0, 0, 1500);

        enemyAircrafts.clear();
        heroBullets.clear();
        enemyBullets.clear();
        props.clear();

        lastEnemyCycle = 0;
        lastHeroShoot = 0;
        lastDifficultyIncrease = 0;
        currentTime = 0;

        gameOver = false;
        localGameOver = false;
        gameOverNotified = false;

        bossOnScreen = false;
        score = 0;
        coins = 0;

        enemyScore = 0;
        enemyGameOver = false;
        lastSentScore = -1;

        hasShield = false;
        hasDoubleGun = false;
        doubleGunEndTime = 0;

        waitingForOpponent = false;

        executorService = new ScheduledThreadPoolExecutor(1);
    }

    public void releaseAudio() {
        if (audioManager != null) {
            audioManager.release();
        }
    }

    public void releaseNetwork() {
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }

            if (reader != null) {
                reader.close();
                reader = null;
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;

        coinDAO = new CoinDAO(getContext());
        hasShield = coinDAO.consumeBuff("shield");

        if (coinDAO.consumeBuff("double_gun")) {
            hasDoubleGun = true;
            doubleGunEndTime = DOUBLE_GUN_DURATION;
            heroShootInterval = baseHeroShootInterval / 2;
        }

        if (musicOn) {
            audioManager.playBgm();
        }

        if (isMultiplayer) {
            waitingForOpponent = true;
            connectToServerAndWait();
        }

        gameThread = new Thread(this);
        gameThread.start();
    }

    private void connectToServerAndWait() {
        new Thread(() -> {
            try {
                System.out.println("[联机] 准备连接 BattleServer: " + serverIp + ":" + BATTLE_PORT);

                socket = new java.net.Socket();
                socket.connect(new java.net.InetSocketAddress(serverIp, BATTLE_PORT), 10000);

                writer = new java.io.PrintWriter(
                        new java.io.BufferedWriter(
                                new java.io.OutputStreamWriter(socket.getOutputStream(), "UTF-8")
                        ),
                        true
                );

                reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(socket.getInputStream(), "UTF-8")
                );

                System.out.println("[联机] 已连接服务器，等待对手...");

                String msg;
                while (isRunning && (msg = reader.readLine()) != null) {
                    System.out.println("[联机] 收到: " + msg);

                    String trimMsg = msg.trim();

                    if ("action:start".equals(trimMsg)) {
                        waitingForOpponent = false;
                        System.out.println("[联机] 对手就绪，游戏开始！");

                    } else if (trimMsg.startsWith("score:")) {
                        try {
                            enemyScore = Integer.parseInt(trimMsg.substring(6).trim());
                        } catch (NumberFormatException ignored) {
                        }

                    } else if ("action:gameover".equals(trimMsg)) {
                        enemyGameOver = true;
                        System.out.println("[联机] 对手死亡");

                        if (localGameOver) {
                            triggerGameOver();
                        }
                    }
                }

                System.out.println("[联机] 服务器连接结束");

            } catch (Exception e) {
                System.out.println("[联机] 连接异常: " + e.getMessage());

                /*
                 * 关键修改：
                 *
                 * 以前这里是：
                 *   waitingForOpponent = false;
                 *
                 * 这会导致连接失败后直接进入单机游戏，
                 * 看起来像“加入方已经开始游戏”，但其实根本没联机成功。
                 *
                 * 现在连接失败后继续停留在等待状态，并提示错误。
                 */
                waitingForOpponent = true;

                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(
                            getContext(),
                            "联机服务器连接失败：" + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        }).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;

        AbstractFlyingObject.setScreenSize(screenWidth, screenHeight);

        if (heroAircraft != null) {
            heroAircraft.setLocationX(screenWidth / 2);
            heroAircraft.setLocationY(
                    screenHeight - ImageManager.HERO_IMAGE.getHeight() / 2 - 20
            );
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        waitingForOpponent = false;

        releaseNetwork();
        particleSystem.clear();

        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }

        audioManager.release();

        if (executorService != null) {
            executorService.shutdown();
        }

        if (gameThread != null) {
            try {
                gameThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();

        while (isRunning) {

            if (waitingForOpponent) {
                drawWaiting();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }

                lastTime = System.currentTimeMillis();
                continue;
            }

            synchronized (pauseLock) {
                while (isPaused && isRunning) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }

            if (!isRunning) {
                break;
            }

            long now = System.currentTimeMillis();
            long delta = now - lastTime;

            if (delta > 0) {
                currentTime += delta;
                updateGame(delta);
                drawGame();
                lastTime = now;
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void drawWaiting() {
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();

        if (canvas == null) {
            return;
        }

        try {
            canvas.drawColor(Color.BLACK);

            paint.setColor(Color.WHITE);
            paint.setTextSize(55);

            String text = "等待对手加入...";
            float tw = paint.measureText(text);

            canvas.drawText(
                    text,
                    (screenWidth - tw) / 2f,
                    screenHeight / 2f - 40,
                    paint
            );

            paint.setTextSize(36);
            paint.setColor(Color.GRAY);

            String sub = "对手进入后自动开始";
            tw = paint.measureText(sub);

            canvas.drawText(
                    sub,
                    (screenWidth - tw) / 2f,
                    screenHeight / 2f + 30,
                    paint
            );

            paint.setTextSize(28);
            paint.setColor(Color.LTGRAY);

            String ipText = "BattleServer: " + serverIp + ":" + BATTLE_PORT;
            tw = paint.measureText(ipText);

            canvas.drawText(
                    ipText,
                    (screenWidth - tw) / 2f,
                    screenHeight / 2f + 90,
                    paint
            );

        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void pauseGame() {
        synchronized (pauseLock) {
            isPaused = true;
        }
    }

    public void resumeGame() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    private void updateGame(long deltaMs) {
        if (gameOver || localGameOver) {
            return;
        }

        if (hasDoubleGun && currentTime >= doubleGunEndTime) {
            hasDoubleGun = false;
            heroShootInterval = baseHeroShootInterval;
        }

        if (currentTime - lastEnemyCycle >= cycleDuration) {
            lastEnemyCycle = currentTime;

            if (("normal".equals(difficulty) || "hard".equals(difficulty))
                    && currentTime - lastDifficultyIncrease >= 15000) {
                lastDifficultyIncrease = currentTime;
                onDifficultyIncrease();
            }

            if (enemyAircrafts.size() < enemyMaxNumber) {
                spawnEnemyByRandom();
            }

            if (!bossOnScreen && shouldGenerateBoss()) {
                bossOnScreen = true;
                summonBoss(800);
            }

            for (AbstractAircraft enemy : enemyAircrafts) {
                if (enemyBullets.size() < MAX_ENEMY_BULLETS) {
                    enemyBullets.addAll(enemy.shoot());
                }
            }
        }

        if (currentTime - lastHeroShoot >= heroShootInterval) {
            lastHeroShoot = currentTime;

            if (heroBullets.size() < MAX_HERO_BULLETS) {
                heroBullets.addAll(heroAircraft.shoot());
            }

            if (musicOn) {
                audioManager.playShootSound();
            }
        }

        for (AbstractAircraft e : enemyAircrafts) {
            e.forward();
        }

        for (BaseBullet b : heroBullets) {
            b.forward();
        }

        for (BaseBullet b : enemyBullets) {
            b.forward();
        }

        for (AbstractProp p : props) {
            p.forward();
        }

        crashCheck();

        for (int i = enemyBullets.size() - 1; i >= 0; i--) {
            if (enemyBullets.get(i).notValid()) {
                enemyBullets.remove(i);
            }
        }

        for (int i = heroBullets.size() - 1; i >= 0; i--) {
            if (heroBullets.get(i).notValid()) {
                heroBullets.remove(i);
            }
        }

        for (int i = enemyAircrafts.size() - 1; i >= 0; i--) {
            if (enemyAircrafts.get(i).notValid()) {
                enemyAircrafts.remove(i);
            }
        }

        for (int i = props.size() - 1; i >= 0; i--) {
            if (props.get(i).notValid()) {
                props.remove(i);
            }
        }

        if (bossOnScreen && enemyAircrafts.stream().noneMatch(e -> e instanceof BossEnemy)) {
            bossOnScreen = false;

            if (musicOn) {
                audioManager.playBgm();
            }
        }

        if (isMultiplayer && writer != null && score != lastSentScore) {
            writer.println("score:" + score);
            lastSentScore = score;
        }

        if (heroAircraft.getHp() <= 0) {
            if (isMultiplayer) {
                if (!localGameOver) {
                    localGameOver = true;

                    if (musicOn) {
                        audioManager.playGameOverSound();
                        audioManager.stopBgm();
                    }

                    if (writer != null) {
                        writer.println("action:gameover");
                    }

                    if (enemyGameOver) {
                        triggerGameOver();
                    }
                }
            } else {
                triggerGameOver();
            }
        }
    }

    private synchronized void triggerGameOver() {
        if (gameOverNotified) {
            return;
        }

        gameOver = true;
        gameOverNotified = true;

        if (!isMultiplayer) {
            if (musicOn) {
                audioManager.playGameOverSound();
                audioManager.stopBgm();
            }
        }

        if (coinDAO != null && coins > 0) {
            coinDAO.addCoins(coins);
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            if (gameOverListener != null) {
                gameOverListener.onGameOver(score, difficulty);
            }
        });
    }

    private void onDifficultyIncrease() {
        enemyMaxNumber = Math.min(enemyMaxNumber + 1, 12);

        if (cycleDuration > 300) {
            cycleDuration -= 50;
        }

        if ("normal".equals(difficulty) || "hard".equals(difficulty)) {
            superEliteSpeedFactor = Math.min(
                    superEliteSpeedFactor + 0.1f,
                    MAX_SUPER_ELITE_FACTOR
            );
        }
    }

    private boolean shouldGenerateBoss() {
        return score >= bossThreshold && !bossOnScreen;
    }

    private void summonBoss(int hp) {
        if (musicOn) {
            audioManager.playBossBgm();
        }

        enemyAircrafts.add(
                new BossEnemy(screenWidth / 2, 80, 0, 5, hp, 100)
        );
    }

    private void spawnEnemyByRandom() {
        EnemyFactory factory;
        double rand = Math.random();

        if (rand < 0.1) {
            factory = new SuperEliteEnemyFactory();
        } else if (rand < 0.4) {
            factory = new EliteEnemyFactory();
        } else {
            factory = new MobEnemyFactory();
        }

        AbstractEnemy enemy = (AbstractEnemy) factory.createEnemy(
                (int) (Math.random() * (screenWidth - 70)),
                (int) (Math.random() * screenHeight * 0.05)
        );

        if (enemy instanceof SuperEliteEnemy) {
            ((SuperEliteEnemy) enemy).setSpeedFactor(superEliteSpeedFactor);
        }

        enemyAircrafts.add(enemy);
    }

    private void crashCheck() {
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }

            if (Math.abs(bullet.getLocationX() - heroAircraft.getLocationX()) < COLLISION_DISTANCE
                    && Math.abs(bullet.getLocationY() - heroAircraft.getLocationY()) < COLLISION_DISTANCE) {

                if (heroAircraft.crash(bullet)) {
                    if (hasShield) {
                        hasShield = false;
                        bullet.vanish();
                    } else {
                        heroAircraft.decreaseHp(bullet.getPower());
                        bullet.vanish();
                    }
                }
            }
        }

        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }

            for (AbstractAircraft enemy : enemyAircrafts) {
                if (enemy.notValid()) {
                    continue;
                }

                if (Math.abs(bullet.getLocationX() - enemy.getLocationX()) < COLLISION_DISTANCE
                        && Math.abs(bullet.getLocationY() - enemy.getLocationY()) < COLLISION_DISTANCE) {

                    if (enemy.crash(bullet)) {
                        enemy.decreaseHp(bullet.getPower());
                        bullet.vanish();

                        if (enemy.notValid()) {
                            if (musicOn) {
                                audioManager.playHitSound();
                            }

                            if (enemy instanceof AbstractEnemy) {
                                score += ((AbstractEnemy) enemy).getScore();
                            }

                            String type = "mob";

                            if (enemy instanceof BossEnemy) {
                                type = "boss";
                            } else if (enemy instanceof EliteEnemy) {
                                type = "elite";
                            } else if (enemy instanceof SuperEliteEnemy) {
                                type = "elite";
                            }

                            particleSystem.emit(
                                    enemy.getLocationX(),
                                    enemy.getLocationY(),
                                    type
                            );

                            if (enemy instanceof BossEnemy) {
                                coins += CoinDAO.COIN_BOSS;
                            } else if (enemy instanceof SuperEliteEnemy) {
                                coins += CoinDAO.COIN_SUPER_ELITE;
                            } else if (enemy instanceof EliteEnemy) {
                                coins += CoinDAO.COIN_ELITE;
                            } else {
                                coins += CoinDAO.COIN_MOB;
                            }

                            dropPropWhenEnemyDie(enemy);
                        }
                    }
                }
            }
        }

        for (AbstractAircraft enemy : enemyAircrafts) {
            if (enemy.notValid()) {
                continue;
            }

            if (Math.abs(enemy.getLocationX() - heroAircraft.getLocationX()) < COLLISION_DISTANCE
                    && Math.abs(enemy.getLocationY() - heroAircraft.getLocationY()) < COLLISION_DISTANCE) {

                if (enemy.crash(heroAircraft) || heroAircraft.crash(enemy)) {
                    enemy.vanish();

                    if (hasShield) {
                        hasShield = false;
                    } else {
                        heroAircraft.decreaseHp(Integer.MAX_VALUE);
                    }
                }
            }
        }

        for (AbstractProp prop : props) {
            if (prop.notValid()) {
                continue;
            }

            if (Math.abs(prop.getLocationX() - heroAircraft.getLocationX()) < COLLISION_DISTANCE
                    && Math.abs(prop.getLocationY() - heroAircraft.getLocationY()) < COLLISION_DISTANCE) {

                if (prop.crash(heroAircraft)) {
                    if (musicOn) {
                        audioManager.playSupplySound();
                    }

                    if (prop instanceof BombProp) {
                        BombProp bomb = (BombProp) prop;

                        for (AbstractAircraft e : enemyAircrafts) {
                            if (e instanceof AbstractEnemy && !(e instanceof BossEnemy)) {
                                bomb.addObserver((AbstractEnemy) e);
                            }
                        }

                        for (BaseBullet b : enemyBullets) {
                            if (b instanceof EnemyBullet) {
                                bomb.addObserver((EnemyBullet) b);
                            }
                        }

                        bomb.activate(heroAircraft);

                        if (musicOn) {
                            audioManager.playBombSound();
                        }
                    } else {
                        prop.activate(heroAircraft);
                    }

                    prop.vanish();
                }
            }
        }
    }

    private void dropPropWhenEnemyDie(AbstractAircraft enemy) {
        PropFactory factory = null;

        if (enemy instanceof EliteEnemy) {
            if (Math.random() < 0.8) {
                factory = randomPropFactory();
            }

        } else if (enemy instanceof SuperEliteEnemy) {
            factory = randomPropFactory();

        } else if (enemy instanceof BossEnemy) {
            int n = new Random().nextInt(3) + 1;

            for (int i = 0; i < n; i++) {
                PropFactory f = randomPropFactory();

                if (f != null) {
                    props.add(
                            f.createProp(
                                    enemy.getLocationX() + i * 20,
                                    enemy.getLocationY() + i * 10
                            )
                    );
                }
            }

            return;
        }

        if (factory != null) {
            props.add(
                    factory.createProp(
                            enemy.getLocationX(),
                            enemy.getLocationY()
                    )
            );
        }
    }

    private PropFactory randomPropFactory() {
        double r = Math.random();

        if (r < 0.25) {
            return new BloodPropFactory();
        }

        if (r < 0.5) {
            return new FirePropFactory();
        }

        if (r < 0.75) {
            return new SuperFirePropFactory();
        }

        return new BombPropFactory();
    }

    public interface GameOverListener {
        void onGameOver(int score, String difficulty);
    }

    private GameOverListener gameOverListener;

    public void setGameOverListener(GameOverListener l) {
        this.gameOverListener = l;
    }

    private void drawGame() {
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();

        if (canvas == null) {
            return;
        }

        try {
            int bgW = backgroundImage.getWidth();
            int bgH = backgroundImage.getHeight();

            float scale = (float) screenHeight / bgH;

            int sW = (int) (bgW * scale);
            int sH = (int) (bgH * scale);

            backGroundTop += 4;

            if (backGroundTop >= sH) {
                backGroundTop = 0;
            }

            int left = (screenWidth - sW) / 2;
            int right = left + sW;

            canvas.drawBitmap(
                    backgroundImage,
                    null,
                    new Rect(left, backGroundTop - sH, right, backGroundTop),
                    null
            );

            canvas.drawBitmap(
                    backgroundImage,
                    null,
                    new Rect(left, backGroundTop, right, backGroundTop + sH),
                    null
            );

            for (BaseBullet b : enemyBullets) {
                drawBitmapCentered(canvas, b.getImage(), b.getLocationX(), b.getLocationY());
            }

            for (BaseBullet b : heroBullets) {
                drawBitmapCentered(canvas, b.getImage(), b.getLocationX(), b.getLocationY());
            }

            for (AbstractAircraft e : enemyAircrafts) {
                drawBitmapCentered(canvas, e.getImage(), e.getLocationX(), e.getLocationY());
            }

            for (AbstractProp p : props) {
                drawBitmapCentered(canvas, p.getImage(), p.getLocationX(), p.getLocationY());
            }

            if (hasShield) {
                paint.setColor(Color.argb(120, 100, 180, 255));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(6);

                canvas.drawCircle(
                        heroAircraft.getLocationX(),
                        heroAircraft.getLocationY(),
                        60,
                        paint
                );

                paint.setStyle(Paint.Style.FILL);
            }

            drawBitmapCentered(
                    canvas,
                    ImageManager.HERO_IMAGE,
                    heroAircraft.getLocationX(),
                    heroAircraft.getLocationY()
            );

            particleSystem.updateAndDraw(canvas);

            paint.setColor(Color.RED);
            paint.setTextSize(50);

            canvas.drawText("Score: " + score, 20, 80, paint);
            canvas.drawText("Life: " + heroAircraft.getHp(), 20, 140, paint);

            paint.setColor(Color.rgb(255, 215, 0));
            canvas.drawText("💰 " + coins, 20, 200, paint);

            if (hasDoubleGun) {
                paint.setColor(Color.rgb(255, 140, 0));
                canvas.drawText("🔥 " + (doubleGunEndTime - currentTime) / 1000 + "s", 20, 260, paint);
            }

            if (hasShield) {
                paint.setColor(Color.rgb(100, 180, 255));
                canvas.drawText("🛡", 20, 320, paint);
            }

            if (isMultiplayer) {
                paint.setColor(Color.YELLOW);
                canvas.drawText("对手: " + enemyScore, screenWidth - 360, 80, paint);
            }

            if (gameOver || localGameOver) {
                paint.setColor(Color.argb(180, 0, 0, 0));
                canvas.drawRect(0, 0, screenWidth, screenHeight, paint);

                paint.setColor(Color.RED);
                paint.setTextSize(100);

                String t = "GAME OVER";
                float tw = paint.measureText(t);

                canvas.drawText(t, (screenWidth - tw) / 2, screenHeight / 2, paint);

                paint.setTextSize(60);
                paint.setColor(Color.WHITE);

                String st = "Score: " + score;
                tw = paint.measureText(st);

                canvas.drawText(st, (screenWidth - tw) / 2, screenHeight / 2 + 100, paint);

                if (isMultiplayer && localGameOver && !gameOver) {
                    paint.setColor(Color.YELLOW);
                    paint.setTextSize(42);

                    String w = "等待对手结束...";
                    tw = paint.measureText(w);

                    canvas.drawText(w, (screenWidth - tw) / 2, screenHeight / 2 + 170, paint);
                }
            }

        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBitmapCentered(Canvas canvas, Bitmap bmp, float x, float y) {
        if (bmp != null) {
            canvas.drawBitmap(
                    bmp,
                    x - bmp.getWidth() / 2f,
                    y - bmp.getHeight() / 2f,
                    null
            );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver || localGameOver || waitingForOpponent) {
            return true;
        }

        int halfW = ImageManager.HERO_IMAGE.getWidth() / 2;
        int halfH = ImageManager.HERO_IMAGE.getHeight() / 2;

        heroAircraft.setLocationX(
                (int) Math.min(
                        Math.max(event.getX(), halfW),
                        screenWidth - halfW
                )
        );

        heroAircraft.setLocationY(
                (int) Math.min(
                        Math.max(event.getY(), halfH),
                        screenHeight - halfH
                )
        );

        return true;
    }

    public void setMusicOn(boolean on) {
        this.musicOn = on;
    }
}