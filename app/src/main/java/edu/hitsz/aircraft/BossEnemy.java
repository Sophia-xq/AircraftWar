package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.CircleShootStrategy;

import java.util.List;

/**
 * Boss敌机：左右移动 + 环形弹道（20发）
 */
public class BossEnemy extends AbstractEnemy {

    private boolean movingRight = true;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int score) {
        super(locationX, locationY, speedX, speedY, hp, score);
        // 使用环射策略（敌机 -> false）
        this.shootStrategy = new CircleShootStrategy(false);
        this.shootInterval = 2500;
    }

    @Override
    public List<BaseBullet> shoot() {
        // 调用策略执行射击逻辑（环射20发）
        return shootStrategy.doShoot(
                this.getLocationX(),
                this.getLocationY() + 20,
                this.getSpeedX(),
                this.getSpeedY(),
                20 // 威力
        );
    }

    @Override
    public void forward() {
        int screenWidth = AbstractFlyingObject.getScreenWidth();

        // 左右漂浮移动逻辑
        if (movingRight) {
            locationX += speedX;
            if (locationX >= screenWidth - 100) {
                movingRight = false;
            }
        } else {
            locationX -= speedX;
            if (locationX <= 100) {
                movingRight = true;
            }
        }

        // 悬浮上下轻微移动
        locationY += Math.random() > 0.5 ? 1 : -1;
    }

    @Override
    public void update() {
        // Boss 不受炸弹影响
        System.out.println("BossEnemy 不受炸弹影响！");
    }
}