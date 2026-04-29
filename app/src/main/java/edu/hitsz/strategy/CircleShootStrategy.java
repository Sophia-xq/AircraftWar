package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import java.util.LinkedList;
import java.util.List;

public class CircleShootStrategy implements ShootStrategy {
    private boolean isHero;

    public CircleShootStrategy(boolean isHero) {
        this.isHero = isHero;
    }

    @Override
    public List<BaseBullet> doShoot(int x, int y, int speedX, int speedY, int power) {
        List<BaseBullet> res = new LinkedList<>();
        int bulletNum = 20;
        int baseSpeed = 5;

        for (int i = 0; i < bulletNum; i++) {
            double angle = 2 * Math.PI / bulletNum * i;
            int vx = (int) (baseSpeed * Math.cos(angle));
            int vy = (int) (baseSpeed * Math.sin(angle));
            if (isHero)
                res.add(new HeroBullet(x, y, vx, vy, power));
            else
                res.add(new EnemyBullet(x, y, vx, vy, power));
        }
        return res;
    }
}
