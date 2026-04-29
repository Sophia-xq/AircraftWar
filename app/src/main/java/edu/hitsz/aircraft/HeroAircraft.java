package edu.hitsz.aircraft;

import edu.hitsz.strategy.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.prop.BombProp;


import java.util.List;

/**
 * 英雄飞机（单例模式 + 策略模式）
 * 游戏中只有一架英雄机，由玩家控制
 */
public class HeroAircraft extends AbstractAircraft {

    private ShootStrategy shootStrategy;
    /** 唯一实例 */
    private static HeroAircraft instance = null;

    /** 当前射击策略类型（0：直射，1：散射，2：环射） */
    private int shootMode = 0;

    /** 子弹射击方向 (向上发射：-1) */
    private final int direction = -1;

    // 拥有方法触发炸弹
    public void bombTrigger(BombProp bomb) {
        bomb.activate(this);
    }

    /**
     * 构造函数私有化，防止外部 new
     */
    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 默认直射
        this.setShootStrategy(new StraightShootStrategy(true));
    }

    /**
     * 获取唯一实例（懒汉式单例）
     */
    public static HeroAircraft getInstance(int locationX, int locationY, int speedX, int speedY, int hp) {
        if (instance == null) {
            instance = new HeroAircraft(locationX, locationY, speedX, speedY, hp);
        }
        return instance;
    }
    public static void resetInstance() {
        instance = null;
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不需要自动移动
    }

    public synchronized void setShootStrategy(ShootStrategy strategy) {
        this.shootStrategy = strategy;
    }

    public synchronized ShootStrategy getShootStrategy() {
        return shootStrategy;
    }

    /**
     * 使用当前策略发射子弹
     */
    @Override
    public List<BaseBullet> shoot() {
        return shootStrategy.doShoot(locationX, locationY, speedX, direction * 5, power);
    }

    /**
     * 增加生命值（血量道具）
     */
    public void increaseHp(int increase) {
        this.hp += increase;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }
    }

    /**
     * 改变射击模式（由火力道具触发）
     * 0：直射；1：散射；2：环射
     */
    public void switchShootMode(int mode) {
        this.shootMode = mode;
        switch (mode) {
            case 0:
                this.setShootStrategy(new StraightShootStrategy(true));
                break;
            case 1:
                this.setShootStrategy(new ScatterShootStrategy(true));
                break;
            case 2:
                this.setShootStrategy(new CircleShootStrategy(true));
                break;
            default:
                this.setShootStrategy(new StraightShootStrategy(true));
                break;
        }
    }

    public int getShootMode() {
        return shootMode;
    }

}
