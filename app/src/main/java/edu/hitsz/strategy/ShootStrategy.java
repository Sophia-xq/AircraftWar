package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;
import java.util.List;

public interface ShootStrategy {
    /**
     * 执行射击策略
     * @param x 飞机中心X坐标
     * @param y 飞机中心Y坐标
     * @param speedX 飞机X方向速度
     * @param speedY 飞机Y方向速度
     * @param power 子弹威力
     * @return 子弹列表
     */
    List<BaseBullet> doShoot(int x, int y, int speedX, int speedY, int power);
}
