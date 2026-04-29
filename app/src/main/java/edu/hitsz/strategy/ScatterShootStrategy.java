package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import java.util.LinkedList;
import java.util.List;

public class ScatterShootStrategy implements ShootStrategy {
    private boolean isHero;

    public ScatterShootStrategy(boolean isHero) {
        this.isHero = isHero;
    }

    @Override
    public List<BaseBullet> doShoot(int x, int y, int speedX, int speedY, int power) {
        List<BaseBullet> res = new LinkedList<>();
        int direction = isHero ? -1 : 1;
        int bulletSpeed = 10 * direction;
        int[][] offsets = {{-2, 0}, {0, 0}, {2, 0}};

        for (int[] offset : offsets) {
            int vx = offset[0];
            int vy = bulletSpeed;
            if (isHero)
                res.add(new HeroBullet(x + offset[0] * 10, y + direction * 20, vx, vy, power));
            else
                res.add(new EnemyBullet(x + offset[0] * 10, y + direction * 20, vx, vy, power));
        }
        return res;
    }
}
