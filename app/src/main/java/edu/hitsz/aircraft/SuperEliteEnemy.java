package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.ScatterShootStrategy;

import java.util.List;

/**
 * 超级精英敌机：左右移动，三向散射
 */
public class SuperEliteEnemy extends AbstractEnemy {

    private boolean movingRight = true;
    private float speedFactor = 1.0f;

    public SuperEliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int score) {
        super(locationX, locationY,  5, 7 + (int)(Math.random() * 3), hp, score);
        // 使用散射射击策略（敌机 -> false）
        this.shootStrategy = new ScatterShootStrategy(false);
        this.shootInterval = 3000;
    }

    public void setSpeedFactor(float factor) {
        this.speedFactor = factor;
    }

    @Override
    public List<BaseBullet> shoot() {
        // 调用策略执行射击逻辑（3发散射）
        return shootStrategy.doShoot(
                this.getLocationX(),
                this.getLocationY() + 10,
                this.getSpeedX(),
                this.getSpeedY(),
                15 // 子弹威力
        );
    }

    @Override
    public void forward() {
        int screenWidth = AbstractFlyingObject.getScreenWidth();
        int screenHeight = AbstractFlyingObject.getScreenHeight();

        // 左右移动逻辑
        if (movingRight) {
            locationX += (int)(speedX * speedFactor);
            if (locationX >= screenWidth - 50) {
                movingRight = false;
            }
        } else {
            locationX -= speedX;
            if (locationX <= 50) {
                movingRight = true;
            }
        }

        // 下落
        locationY += (int)(speedY * speedFactor);
        if (locationY >= screenHeight) {
            vanish();
        }
    }

    @Override
    public void update() {
        System.out.println("SuperEliteEnemy 受到炸弹影响，血量减少！");
        decreaseHp(45); // 扣一部分血
        if (getHp() <= 0) vanish();
    }
}