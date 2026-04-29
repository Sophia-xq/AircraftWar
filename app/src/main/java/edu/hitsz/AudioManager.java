package edu.hitsz;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.SparseIntArray;

public class AudioManager {
    private static AudioManager instance;
    private Context context;
    private boolean isMusicOn = true;
    private boolean isSoundOn = true;

    private MediaPlayer bgmMediaPlayer;
    private MediaPlayer bossBgmMediaPlayer;
    private boolean isBossPlaying = false;

    private SoundPool soundPool;
    private SparseIntArray soundMap; // 存储音效ID

    private long lastShootSoundTime = 0;

    private AudioManager(Context context) {
        this.context = context.getApplicationContext();
        initSoundPool();
    }

    public static synchronized AudioManager getInstance(Context context) {
        if (instance == null) {
            instance = new AudioManager(context);
        }
        return instance;
    }

    private void initSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(attributes)
                .build();

        soundMap = new SparseIntArray();
        // 注意：资源 ID 必须与 raw 中的文件名一致（自动生成）
        soundMap.put(1, soundPool.load(context, R.raw.bullet_hit, 1));
        soundMap.put(2, soundPool.load(context, R.raw.bomb_explosion, 1));
        soundMap.put(3, soundPool.load(context, R.raw.get_supply, 1));
        soundMap.put(4, soundPool.load(context, R.raw.game_over, 1));
        soundMap.put(5, soundPool.load(context, R.raw.bullet, 1));   // 射击音效
    }

    public void playBgm() {
        if (!isMusicOn) return;
        stopBgm();
        if (bgmMediaPlayer == null) {
            bgmMediaPlayer = MediaPlayer.create(context, R.raw.bgm);
            bgmMediaPlayer.setLooping(true);
        }
        bgmMediaPlayer.start();
        isBossPlaying = false;
    }

    public void playBossBgm() {
        if (!isMusicOn) return;
        if (isBossPlaying) return;
        stopBgm();
        if (bossBgmMediaPlayer == null) {
            bossBgmMediaPlayer = MediaPlayer.create(context, R.raw.bgm_boss);
            bossBgmMediaPlayer.setLooping(true);
        }
        bossBgmMediaPlayer.start();
        isBossPlaying = true;
    }

    public void stopBgm() {
        if (bgmMediaPlayer != null) {
            if (bgmMediaPlayer.isPlaying()) bgmMediaPlayer.stop();
            bgmMediaPlayer.release();
            bgmMediaPlayer = null;
        }
        if (bossBgmMediaPlayer != null) {
            if (bossBgmMediaPlayer.isPlaying()) bossBgmMediaPlayer.stop();
            bossBgmMediaPlayer.release();
            bossBgmMediaPlayer = null;
        }
        isBossPlaying = false;
    }

    public void playHitSound() {
        if (!isSoundOn) return;
        playSound(1,0.8f);
    }

    public void playBombSound() {
        if (!isSoundOn) return;
        playSound(2,1.0f);
    }

    public void playSupplySound() {
        if (!isSoundOn) return;
        playSound(3,0.8f);
    }

    public void playGameOverSound() {
        if (!isSoundOn) return;
        playSound(4,1.0f);
    }

    public void playShootSound() {
        if (!isSoundOn) return;

        long now = System.currentTimeMillis();
        if (now - lastShootSoundTime > 100) { //限制频率
            playSound(5, 0.2f);
            lastShootSoundTime = now;
        }
    }

    private void playSound(int id, float volume) {
        int soundId = soundMap.get(id);
        if (soundId != 0) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
        }
    }

    public void release() {
        stopBgm();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    public void setMusicOn(boolean on) {
        isMusicOn = on;
        if (!on) stopBgm();
    }

    public void setSoundOn(boolean on) {
        isSoundOn = on;
    }
}