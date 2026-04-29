package edu.hitsz.factory;

import edu.hitsz.aircraft.EliteEnemy;

public class EliteEnemyFactory implements EnemyFactory {

    @Override
    public EliteEnemy createEnemy(int locationX, int locationY) {
        int speedX = 0;
        int speedY = 4;
        int hp = 50;
        int score = 50;
        return new EliteEnemy(locationX, locationY, speedX, speedY, hp, score);
    }
}
