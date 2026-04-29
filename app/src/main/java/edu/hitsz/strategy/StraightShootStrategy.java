package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import java.util.LinkedList;
import java.util.List;

public class StraightShootStrategy implements ShootStrategy {
    private boolean isHero;

    public StraightShootStrategy(boolean isHero) {
        this.isHero = isHero;
    }

    @Override
    public List<BaseBullet> doShoot(int x, int y, int speedX, int speedY, int power) {
        List<BaseBullet> res = new LinkedList<>();
        int direction = isHero ? -1 : 1; // 英雄机向上射，敌机向下射
        int bulletSpeed = 10 * direction;
        if (isHero) {
            res.add(new HeroBullet(x, y + direction * 20, 0, bulletSpeed, power));
        } else {
            res.add(new EnemyBullet(x, y + direction * 20, 0, bulletSpeed, power));
        }
        return res;
    }
}
