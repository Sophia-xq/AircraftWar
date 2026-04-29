package edu.hitsz.factory;

import edu.hitsz.prop.FireProp;

/**
 * 火力道具工厂：生产普通火力道具（散射）
 */
public class FirePropFactory implements PropFactory {

    @Override
    public FireProp createProp(int locationX, int locationY) {
        // 普通火力道具，速度较慢
        return new FireProp(locationX, locationY, 0, 5);
    }
}
