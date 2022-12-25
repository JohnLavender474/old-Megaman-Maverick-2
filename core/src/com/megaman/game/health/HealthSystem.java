package com.megaman.game.health;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;

public class HealthSystem extends System {

    public HealthSystem() {
        super(HealthComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        HealthComponent c = e.getComponent(HealthComponent.class);
        if (c.getHealth() == 0) {
            e.dead = true;
        }
    }

}
