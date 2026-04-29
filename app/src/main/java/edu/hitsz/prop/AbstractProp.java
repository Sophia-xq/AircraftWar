package edu.hitsz.prop;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.aircraft.HeroAircraft;

/**
 * 道具抽象类
 */
public abstract class AbstractProp extends AbstractFlyingObject {

    public AbstractProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    /**
     * 英雄机拾取该道具时触发
     */
    public abstract void activate(HeroAircraft hero);
}
