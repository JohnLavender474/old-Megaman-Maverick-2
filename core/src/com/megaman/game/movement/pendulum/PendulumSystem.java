package com.megaman.game.movement.pendulum;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.UpdatableConsumer;

public class PendulumSystem extends System {

    public PendulumSystem() {
        super(PendulumComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        PendulumComponent pc = e.getComponent(PendulumComponent.class);
        Pendulum p = pc.pendulum;
        if (p == null) {
            return;
        }
        p.update(delta);
        UpdatableConsumer<Pendulum> uc = pc.updatableConsumer;
        if (uc == null) {
            return;
        }
        uc.consumeAndUpdate(p, delta);
    }

}
