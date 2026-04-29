package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;

import java.util.ArrayList;
import java.util.List;

/**
 * 普通敌机：不会射击
 */
public class MobEnemy extends AbstractEnemy {

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp, int score) {
        super(locationX, locationY, speedX,  5 + (int)(Math.random() * 2), hp, score);
    }

    @Override
    public List<BaseBullet> shoot() {
        return new ArrayList<>(); // 普通敌机不射击
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
        System.out.println("MobEnemy 被炸弹摧毁！");
        vanish();
    }
}