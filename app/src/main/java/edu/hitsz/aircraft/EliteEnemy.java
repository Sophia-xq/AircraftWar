package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.StraightShootStrategy;

import java.util.List;

/**
 * 精英敌机：可以射击
 */
public class EliteEnemy extends AbstractEnemy {

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int score) {
        super(locationX, locationY, speedX, 6 + (int)(Math.random() * 3), hp, score);
        // 敌机使用直射策略（向下发射）
        this.shootStrategy = new StraightShootStrategy(false);
        this.shootInterval = 3000;
    }

    @Override
    public List<BaseBullet> shoot() {
        // 调用策略执行射击逻辑
        return shootStrategy.doShoot(
                this.getLocationX(),
                this.getLocationY() + 10,
                0,
                this.getSpeedY(),
                10
        );
    }

    @Override
    public void forward() {
        super.forward();
        int screenHeight = AbstractFlyingObject.getScreenHeight();
        if (locationY >= screenHeight) {
            vanish();
        }
    }

    @Override
    public void update() {
        System.out.println("EliteEnemy 被炸弹摧毁！");
        vanish();
    }
}