package edu.hitsz.effect;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * 单个爆炸粒子
 * 拥有初速度、重力、生命周期，透明度随时间衰减
 */
public class Particle {

    private float x, y;           // 当前位置
    private float vx, vy;         // 速度
    private float alpha;          // 透明度 0~255
    private final float decay;    // 每帧衰减量
    private float radius;         // 粒子半径
    private final int baseColor;  // 基础颜色（RGB部分）
    private static final float GRAVITY = 0.15f; // 重力加速度

    public Particle(float x, float y, float vx, float vy, int color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.baseColor = color;
        this.alpha = 255f;
        // 粒子大小随机 3~7dp
        this.radius = 3f + (float)(Math.random() * 4f);
        // 衰减速度随机：让粒子消失时间有差异，更自然
        this.decay = 4f + (float)(Math.random() * 4f);
    }

    /** 每帧更新物理状态 */
    public void update() {
        x += vx;
        y += vy;
        vy += GRAVITY;   // 重力让粒子向下加速
        vx *= 0.96f;     // 空气阻力减速
        alpha -= decay;
        radius *= 0.97f; // 粒子逐渐缩小
    }

    /** alpha 归零则视为已死亡，可从列表移除 */
    public boolean isDead() {
        return alpha <= 0;
    }

    public void draw(Canvas canvas, Paint paint) {
        int a = (int) Math.max(0, Math.min(255, alpha));
        // 将 baseColor 的 RGB 与当前 alpha 合并
        int color = Color.argb(a,
                Color.red(baseColor),
                Color.green(baseColor),
                Color.blue(baseColor));
        paint.setColor(color);
        canvas.drawCircle(x, y, Math.max(1f, radius), paint);
    }
}