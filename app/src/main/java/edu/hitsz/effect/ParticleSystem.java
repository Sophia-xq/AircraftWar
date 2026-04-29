package edu.hitsz.effect;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

/**
 * 粒子系统管理器
 *
 * 在 GameView 中持有一个实例：
 *   private final ParticleSystem particleSystem = new ParticleSystem();
 *
 * 敌机死亡时调用：
 *   particleSystem.emit(enemy.getLocationX(), enemy.getLocationY(), enemyType);
 *
 * 每帧在 drawGame() 中调用：
 *   particleSystem.updateAndDraw(canvas, paint);
 */
public class ParticleSystem {

    // 控制每次爆炸产生的粒子数，不超过10个防止掉帧
    private static final int PARTICLES_PER_EXPLOSION = 10;

    private final List<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint();

    public ParticleSystem() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    /**
     * 在指定位置触发爆炸粒子
     * @param x         爆炸中心 x
     * @param y         爆炸中心 y
     * @param enemyType 敌机类型字符串，决定粒子颜色
     *                  "mob"   → 橙色
     *                  "elite" → 黄色
     *                  "boss"  → 红色（粒子更多）
     */
    public void emit(float x, float y, String enemyType) {
        int color;
        int count = PARTICLES_PER_EXPLOSION;

        switch (enemyType) {
            case "boss":
                color = Color.rgb(255, 80, 30);
                count = PARTICLES_PER_EXPLOSION * 2; // Boss 爆炸更壮观，但最多20个
                break;
            case "elite":
                color = Color.rgb(255, 220, 50);
                break;
            default: // mob
                color = Color.rgb(255, 140, 0);
                break;
        }

        for (int i = 0; i < count; i++) {
            // 向四周随机散射
            double angle = Math.random() * Math.PI * 2;
            float speed = 2f + (float)(Math.random() * 5f);
            float vx = (float)(Math.cos(angle) * speed);
            float vy = (float)(Math.sin(angle) * speed) - 2f; // 初始略向上

            // 随机微调颜色，让粒子看起来有层次
            int r = Math.min(255, Color.red(color)   + (int)(Math.random() * 40 - 20));
            int g = Math.min(255, Color.green(color) + (int)(Math.random() * 40 - 20));
            int b = Math.min(255, Color.blue(color)  + (int)(Math.random() * 40 - 20));

            particles.add(new Particle(x, y, vx, vy, Color.rgb(r, g, b)));
        }
    }

    /**
     * 每帧调用：更新所有粒子状态并绘制，同时移除已死亡粒子
     * 关键：透明度归零立即 remove，防止内存持续增长
     */
    public void updateAndDraw(Canvas canvas) {
        // 反向遍历，安全移除
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.isDead()) {
                particles.remove(i); // 透明度为0立即移除
            } else {
                p.draw(canvas, paint);
            }
        }
    }

    /** 当前粒子总数（调试用） */
    public int getParticleCount() {
        return particles.size();
    }

    /** 清空所有粒子（切换 Activity 时调用） */
    public void clear() {
        particles.clear();
    }
}