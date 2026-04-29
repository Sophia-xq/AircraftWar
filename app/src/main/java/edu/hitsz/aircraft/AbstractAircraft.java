package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.ShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 所有种类飞机的抽象父类：
 * 敌机（BOSS, ELITE, MOB），英雄飞机
 * 支持策略模式的射击系统
 *
 * @author hitsz
 */
public abstract class AbstractAircraft extends AbstractFlyingObject {

    /**
     * 生命值
     */
    protected int maxHp;
    protected int hp;

    /**
     * 飞机火力值（用于决定子弹伤害）
     */
    protected int power = 10;

    /**
     * 射击策略接口（策略模式核心）
     */
    protected ShootStrategy shootStrategy;
    protected long lastShootTime = 0; // 上次发射时间
    protected int shootInterval = 1000; // 默认1秒


    public AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
    }

    // 血量控制

    public void decreaseHp(int decrease) {
        hp -= decrease;
        if (hp <= 0) {
            hp = 0;
            vanish();
        }
    }

    public int getHp() {
        return hp;
    }

    public int getSpeedX() {
        return speedX;
    }

    public int getSpeedY() {
        return speedY;
    }

    public void setLocationX(int x) {
        this.locationX = x;
    }

    public void setLocationY(int y) {
        this.locationY = y;
    }



    // 策略模式核心

    /**
     * 设置射击策略
     */
    public void setShootStrategy(ShootStrategy strategy) {
        this.shootStrategy = strategy;
    }

    /**
     * 飞机射击方法（模板方法）
     * 调用当前绑定的射击策略
     */
    public List<BaseBullet> shoot() {
        long now = System.currentTimeMillis();
        if (now - lastShootTime < shootInterval) {
            return new LinkedList<>(); // 冷却未到，不发射
        }
        lastShootTime = now; // 更新发射时间
        if (shootStrategy == null) return new LinkedList<>();
        return shootStrategy.doShoot(locationX, locationY, speedX, speedY, power);
    }

}
