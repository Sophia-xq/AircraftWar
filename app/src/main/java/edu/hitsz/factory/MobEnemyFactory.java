package edu.hitsz.factory;

import edu.hitsz.aircraft.MobEnemy;

public class MobEnemyFactory implements EnemyFactory {

    @Override
    public MobEnemy createEnemy(int locationX, int locationY) {
        int speedX = 0;
        int speedY = 4;
        int hp = 30;
        int score = 30;
        return new MobEnemy(locationX, locationY, speedX, speedY, hp, score);
    }
}
