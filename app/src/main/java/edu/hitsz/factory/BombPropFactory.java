package edu.hitsz.factory;

import edu.hitsz.prop.BombProp;

public class BombPropFactory implements PropFactory {
    @Override
    public BombProp createProp(int locationX, int locationY) {
        return new BombProp(locationX, locationY, 0, 5);
    }
}
