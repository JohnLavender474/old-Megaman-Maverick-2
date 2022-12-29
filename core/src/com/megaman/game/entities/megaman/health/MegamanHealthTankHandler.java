package com.megaman.game.entities.megaman.health;

import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.health.HealthVals;

public class MegamanHealthTankHandler {

    private final int[] tanks;
    private final boolean[] has;
    private final Megaman megaman;

    public MegamanHealthTankHandler(Megaman megaman) {
        this.megaman = megaman;
        tanks = new int[MegaHealthTank.values().length];
        has = new boolean[MegaHealthTank.values().length];
    }

    public boolean add(int add) {
        if (add < 0) {
            throw new IllegalStateException("Cannot add negative amount of health");
        }
        boolean added = false;
        for (int i = 0; i < tanks.length; i++) {
            if (add <= 0) {
                break;
            }
            if (!has[i]) {
                continue;
            }
            added = true;
            int t = HealthVals.MAX_HEALTH - tanks[i];
            if (t >= add) {
                tanks[i] += add;
            } else {
                tanks[i] += t;
                add -= t;
            }
        }
        return added;
    }

    public void put(MegaHealthTank healthTank) {
        put(healthTank, 0);
    }

    public void put(MegaHealthTank healthTank, int health) {
        if (health > HealthVals.MAX_HEALTH) {
            health = HealthVals.MAX_HEALTH;
        } else if (health < 0) {
            health = 0;
        }
        has[healthTank.ordinal()] = true;
        tanks[healthTank.ordinal()] = health;
    }

    public boolean has(MegaHealthTank healthTank) {
        return has[healthTank.ordinal()];
    }

    public int get(MegaHealthTank healthTank) {
        if (!has(healthTank)) {
            return 0;
        }
        return tanks[healthTank.ordinal()];
    }

}
