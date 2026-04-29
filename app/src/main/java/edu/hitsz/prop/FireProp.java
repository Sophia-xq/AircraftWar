package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.ScatterShootStrategy;
import edu.hitsz.strategy.StraightShootStrategy;
import edu.hitsz.util.GameScheduler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 火力道具：改变英雄机的射击策略为散射（3发），持续10s结束
 */
public class FireProp extends AbstractProp {
    public static final int DURATION_SECONDS = 10;

    public FireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("FireSupply active! 英雄机切换为散射弹道！");
        // 切换射击策略为散射
        heroAircraft.setShootStrategy(new ScatterShootStrategy(true));

        ScheduledExecutorService exec = GameScheduler.get();
        exec.schedule(() -> {
            heroAircraft.setShootStrategy(new StraightShootStrategy(true));
            System.out.println("散射模式结束，恢复直射。");
        }, DURATION_SECONDS, TimeUnit.SECONDS);
    }
}
