package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.ShootStrategy;
import edu.hitsz.observer.Observer;

import java.util.List;
import java.util.LinkedList;


/**
 * 敌机抽象类，继承自 AbstractAircraft
 * 用于区分英雄机和各种敌机
 */
public abstract class AbstractEnemy extends AbstractAircraft implements Observer {

    /**
     * 击毁该敌机可获得的分数
     */
    protected int score;

    /**
     * 射击策略
     */
    protected ShootStrategy shootStrategy;

    public AbstractEnemy(int locationX, int locationY,
                         int speedX, int speedY,
                         int hp, int score) {
        super(locationX, locationY, speedX, speedY, hp);
        this.score = score;
    }

    @Override
    public void update() {
        // 默认行为：被炸弹影响
        if (this instanceof MobEnemy || this instanceof EliteEnemy) {
            this.vanish();
        } else if (this instanceof SuperEliteEnemy) {
            this.decreaseHp(50); // 扣血逻辑
        } else if (this instanceof BossEnemy) {
            // Boss 不受影响
        }
    }

    /**
     * 获取该敌机对应的分值
     */
    public int getScore() {
        return score;
    }

    /**
     * 设置射击策略
     */
    public void setShootStrategy(ShootStrategy shootStrategy) {
        this.shootStrategy = shootStrategy;
    }


    /**
     * 射击方法，调用策略模式生成子弹
     */
    @Override
    public List<BaseBullet> shoot() {
        long now = System.currentTimeMillis();
        if (now - lastShootTime < shootInterval) {
            return new LinkedList<>(); // 冷却未到，返回空列表
        }
        lastShootTime = now; // 更新最后发射时间

        if (shootStrategy == null) return new LinkedList<>();
        return shootStrategy.doShoot(locationX, locationY, speedX, speedY, power);
    }
}
