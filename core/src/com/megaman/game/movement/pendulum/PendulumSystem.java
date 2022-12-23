package com.megaman.game.movement.pendulum;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;

public class PendulumSystem extends System {

    public PendulumSystem() {
        super(PendulumComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        PendulumComponent c = e.getComponent(PendulumComponent.class);
        if (c.pendulum != null) {
            c.pendulum.update(delta);
        }
        if (c.updatable != null) {
            c.updatable.update(delta);
        }
    }

}
