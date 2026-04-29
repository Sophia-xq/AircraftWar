package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.CircleShootStrategy;
import edu.hitsz.strategy.StraightShootStrategy;
import edu.hitsz.util.GameScheduler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 超级火力道具：改变英雄机的射击策略为环射（20发），持续10s结束
 */
public class SuperFireProp extends AbstractProp {
    private static final int DURATION_SECONDS = 10;

    public SuperFireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("SuperFireSupply active! 英雄机切换为环射弹道！");
        // 切换射击策略为环射
        heroAircraft.setShootStrategy(new CircleShootStrategy(true));
        ScheduledExecutorService exec = GameScheduler.get();
        exec.schedule(() -> {
            heroAircraft.setShootStrategy(new StraightShootStrategy(true));
            System.out.println("[SuperFireProp] 环射模式结束，恢复直射。");
        }, DURATION_SECONDS, TimeUnit.SECONDS);

    }
}
