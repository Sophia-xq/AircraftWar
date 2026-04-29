package edu.hitsz.factory;

import edu.hitsz.aircraft.BossEnemy;

public class BossEnemyFactory implements EnemyFactory {

    @Override
    public BossEnemy createEnemy(int locationX, int locationY) {
        int speedX = 5;
        int speedY = 0;
        int hp = 500;
        int score = 500;
        return new BossEnemy(locationX, locationY, speedX, speedY, hp, score);
    }
}
