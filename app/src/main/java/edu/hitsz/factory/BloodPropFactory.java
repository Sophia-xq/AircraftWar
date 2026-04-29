package edu.hitsz.factory;

import edu.hitsz.prop.BloodProp;

public class BloodPropFactory implements PropFactory {
    @Override
    public BloodProp createProp(int locationX, int locationY) {
        return new BloodProp(locationX, locationY, 0, 5);
    }
}
