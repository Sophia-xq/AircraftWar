package edu.hitsz.bullet;

import edu.hitsz.observer.Observer;

/**
 * @Author hitsz
 */
public class EnemyBullet extends BaseBullet implements Observer {

    public EnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
    }
    @Override
    public void update() {
        System.out.println("EnemyBullet 被炸弹清除！");
        this.vanish();
    }

}
