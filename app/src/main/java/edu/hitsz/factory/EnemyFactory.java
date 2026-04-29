package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractEnemy;

public interface EnemyFactory {
    /**
     * 创建敌机
     *
     * @param locationX X 坐标
     * @param locationY Y 坐标
     * @return AbstractEnemy 实例
     */
    AbstractEnemy createEnemy(int locationX, int locationY);
}
