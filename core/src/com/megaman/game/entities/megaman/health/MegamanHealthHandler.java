package com.megaman.game.entities.megaman.health;

import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.health.HealthComponent;
import com.megaman.game.health.HealthVals;

import java.util.Arrays;

public class MegamanHealthHandler {

    private final Megaman megaman;
    private final int[] healthTanks;
    private final boolean[] hasHealthTank;

    public MegamanHealthHandler(Megaman megaman) {
        this.megaman = megaman;
        healthTanks = new int[MegamanHealthTank.values().length];
        hasHealthTank = new boolean[MegamanHealthTank.values().length];
    }

    public void putHealthTank(MegamanHealthTank healthTank) {
        putHealthTank(healthTank, 0);
    }

    public void putHealthTank(MegamanHealthTank healthTank, int health) {
        if (health > HealthVals.MAX_HEALTH) {
            health = HealthVals.MAX_HEALTH;
        } else if (health < 0) {
            health = 0;
        }
        healthTanks[healthTank.ordinal()] = health;
        hasHealthTank[healthTank.ordinal()] = true;
    }

    public boolean hasHealthTank(MegamanHealthTank healthTank) {
        return hasHealthTank[healthTank.ordinal()];
    }

    public int addHealth(int health) {
        int toAddToMegaman = HealthVals.MAX_HEALTH - health;
        int diff = megaman.getComponent(HealthComponent.class).translateHealth(toAddToMegaman);
        for (int i = 0; i < MegamanHealthTank.values().length; i++) {
            if (diff <= 0) {
                break;
            }
            if (!hasHealthTank[i]) {
                continue;
            }
            int toAdd = Integer.min(diff, HealthVals.MAX_HEALTH - healthTanks[i]);
            healthTanks[i] += toAdd;
            diff -= toAdd;
        }
        return Integer.max(diff, 0);
    }

    public void removeHealth(int damage) {
        if (damage < 0) {
            throw new IllegalStateException("Damage cannot be negative");
        }
        megaman.getComponent(HealthComponent.class).translateHealth(-damage);
    }

    public void clearHealthTanks() {
        Arrays.fill(healthTanks, 0);
        Arrays.fill(hasHealthTank, false);
    }

}
