package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 加血道具
 */
public class BloodProp extends AbstractProp {

    /** 恢复的血量 */
    private int heal = 30;

    public BloodProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("BloodProp activated! 英雄机回血 " + heal);
        heroAircraft.increaseHp(heal);
    }
}
