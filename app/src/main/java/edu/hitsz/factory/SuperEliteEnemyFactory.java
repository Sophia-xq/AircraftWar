package edu.hitsz.factory;

import edu.hitsz.aircraft.SuperEliteEnemy;

public class SuperEliteEnemyFactory implements EnemyFactory {

    @Override
    public SuperEliteEnemy createEnemy(int locationX, int locationY) {
        int speedX = 5;
        int speedY = 2;
        int hp = 90;
        int score = 100;
        return new SuperEliteEnemy(locationX, locationY, speedX, speedY, hp, score);
    }
}
