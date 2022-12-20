package com.megaman.game.health;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;
import lombok.Getter;

public class HealthComponent implements Component {

    public Array<Runnable> runOnDeath = new Array<>();

    @Getter
    private int health;

    public HealthComponent(Runnable... runOnDeath) {
        this.runOnDeath.addAll(runOnDeath);
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
