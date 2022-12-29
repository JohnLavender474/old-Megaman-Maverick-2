package com.megaman.game.health;

import com.megaman.game.Component;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

@NoArgsConstructor
public class HealthComponent implements Component {

    @Getter
    private int health;
    @Setter
    private Supplier<Integer> maxHealthSupplier;

    public HealthComponent(Supplier<Integer> maxHealthSupplier) {
        this.maxHealthSupplier = maxHealthSupplier;
    }

    public void setDead() {
        setHealth(0);
    }

    public void setHealth(int h) {
        int max;
        if (maxHealthSupplier != null) {
            max = maxHealthSupplier.get();
        } else {
            max = HealthVals.MAX_HEALTH;
        }
        if (h > max) {
            h = max;
        } else if (h < 0) {
            h = 0;
        }
        health = h;
    }

    public int translateHealth(int delta) {
        int diff = 0;
        health += delta;
        int max;
        if (maxHealthSupplier != null) {
            max = maxHealthSupplier.get();
        } else {
            max = HealthVals.MAX_HEALTH;
        }
        if (health > max) {
            diff = health - max;
            health = max;
        } else if (health < 0) {
            diff = -health;
            health = 0;
        }
        return diff;
    }

    @Override
    public void reset() {
        if (maxHealthSupplier != null) {
            health = maxHealthSupplier.get();
        } else {
            health = HealthVals.MAX_HEALTH;
        }
    }

}
