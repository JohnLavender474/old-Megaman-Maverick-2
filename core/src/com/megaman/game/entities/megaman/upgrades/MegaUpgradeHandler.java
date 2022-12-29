package com.megaman.game.entities.megaman.upgrades;

import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.health.HealthVals;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class MegaUpgradeHandler {

    private final Megaman megaman;
    private final Set<MegaAbility> abilities;
    private final Set<MegaHeartTank> heartTanks;
    private final Set<MegaArmorPiece> armorPieces;
    private final Map<MegaHealthTank, Integer> healthTanks;

    public MegaUpgradeHandler(Megaman megaman) {
        this.megaman = megaman;
        abilities = EnumSet.noneOf(MegaAbility.class);
        heartTanks = EnumSet.noneOf(MegaHeartTank.class);
        armorPieces = EnumSet.noneOf(MegaArmorPiece.class);
        healthTanks = new EnumMap<>(MegaHealthTank.class);
    }

    public boolean has(MegaHeartTank heartTank) {
        return heartTanks.contains(heartTank);
    }

    public boolean has(MegaAbility ability) {
        return abilities.contains(ability);
    }

    public boolean has(MegaArmorPiece armorPiece) {
        return armorPieces.contains(armorPiece);
    }

    public void add(MegaHeartTank heartTank) {
        if (has(heartTank)) {
            return;
        }
        heartTanks.add(heartTank);
        megaman.setMaxHealth(megaman.getMaxHealth() + MegaHeartTank.HEALTH_BUMP);
    }

    public void add(MegaAbility ability) {
        abilities.add(ability);
    }

    public void add(MegaArmorPiece armorPiece) {
        if (has(armorPiece)) {
            return;
        }
        armorPieces.add(armorPiece);

        // TODO: event on add armor piece

    }

    public boolean add(int health) {
        if (health < 0) {
            throw new IllegalStateException("Cannot add negative amount of health");
        }
        boolean added = false;
        for (Map.Entry<MegaHealthTank, Integer> e : healthTanks.entrySet()) {
            if (health <= 0) {
                break;
            }
            added = true;
            int t = HealthVals.MAX_HEALTH - e.getValue();
            if (t >= health) {
                e.setValue(e.getValue() + health);
            } else {
                e.setValue(e.getValue() + t);
                health -= t;
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
        healthTanks.put(healthTank, health);
    }

    public boolean has(MegaHealthTank healthTank) {
        return healthTanks.containsKey(healthTank);
    }

    public int get(MegaHealthTank healthTank) {
        if (!has(healthTank)) {
            return 0;
        }
        return healthTanks.get(healthTank);
    }

}
