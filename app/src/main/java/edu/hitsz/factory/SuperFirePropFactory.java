package edu.hitsz.factory;

import edu.hitsz.prop.SuperFireProp;

/**
 * 超级火力道具工厂：生产超级火力道具（环射）
 */
public class SuperFirePropFactory implements PropFactory {

    @Override
    public SuperFireProp createProp(int locationX, int locationY) {
        // 超级火力道具，下降速度略快
        return new SuperFireProp(locationX, locationY, 0, 6);
    }
}
