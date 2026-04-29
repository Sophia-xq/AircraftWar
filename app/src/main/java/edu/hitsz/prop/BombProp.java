package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.observer.Observer;
import edu.hitsz.observer.Subject;

import java.util.ArrayList;
import java.util.List;

public class BombProp extends AbstractProp implements Subject {

    private final List<Observer> observers = new ArrayList<>();

    public BombProp(int locationX, int locationY, int width, int height) {
        super(locationX, locationY, 0, 5);
    }

    @Override
    public void addObserver(Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyAllObservers() {
        for (Observer o : observers) {
            o.update();
        }
        observers.clear(); // 通知后清空，避免重复
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {

        System.out.println("[炸弹生效] 通知所有敌机和敌机子弹！");
        notifyAllObservers();
        // 道具自己消失
        this.vanish();
    }
}
