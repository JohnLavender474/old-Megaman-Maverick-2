package com.megaman.game.health;

import com.megaman.game.Component;
import lombok.Getter;

public class HealthComponent implements Component {

    @Getter
    private int health = HealthVals.MAX_HEALTH;

    public void setDead() {
        setHealth(0);
    }

    public void setHealth(int h) {
        if (h > HealthVals.MAX_HEALTH) {
            h = HealthVals.MAX_HEALTH;
        } else if (h < 0) {
            h = 0;
        }
        health = h;
    }

    public int translateHealth(int delta) {
        int diff = 0;
        health += delta;
        if (health > HealthVals.MAX_HEALTH) {
            diff = health - HealthVals.MAX_HEALTH;
            health = HealthVals.MAX_HEALTH;
        } else if (health < 0) {
            diff = -health;
            health = 0;
        }
        return diff;
    }

    @Override
    public void reset() {
        health = HealthVals.MAX_HEALTH;
    }

}
