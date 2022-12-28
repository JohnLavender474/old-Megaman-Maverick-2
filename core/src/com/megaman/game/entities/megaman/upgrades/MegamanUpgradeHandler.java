package com.megaman.game.entities.megaman.upgrades;

import com.megaman.game.entities.megaman.Megaman;

import java.util.EnumSet;
import java.util.Set;

public class MegamanUpgradeHandler {

    private final Megaman megaman;
    private final Set<MegamanAbility> abilities;

    public MegamanUpgradeHandler(Megaman megaman) {
        this.megaman = megaman;
        abilities = EnumSet.noneOf(MegamanAbility.class);
    }

    public boolean has(MegamanAbility ability) {
        return abilities.contains(ability);
    }

    public void add(MegamanAbility ability) {
        abilities.add(ability);
    }

    public void remove(MegamanAbility ability) {
        abilities.remove(ability);
    }

}
