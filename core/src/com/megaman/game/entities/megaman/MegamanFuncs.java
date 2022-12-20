package com.megaman.game.entities.megaman;

import com.megaman.game.components.ComponentType;
import com.megaman.game.health.HealthComponent;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class MegamanFuncs {

    private final MegamanStats megamanStats;
    private final Supplier<Megaman> megamanSupplier;

    public boolean useHealthTank(int healthTank) {
        Megaman megaman = megamanSupplier.get();
        if (megaman == null || !megamanStats.hasHealthTank(healthTank)) {
            return false;
        }
        int val = megamanStats.getHealthTankValue(healthTank);
        ((HealthComponent) megaman.getComponent(ComponentType.HEALTH)).health += val;
        megamanStats.setHealthTankValue(healthTank, 0);
        return true;
    }

}
