package com.megaman.game.movement.rotatingline;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.UpdatableConsumer;

public class RotatingLineSystem extends System {

    public RotatingLineSystem() {
        super(RotatingLineComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        RotatingLineComponent c = e.getComponent(RotatingLineComponent.class);
        RotatingLine r = c.rotatingLine;
        if (r != null) {
            r.update(delta);
        }
        UpdatableConsumer<RotatingLine> u = c.updatableConsumer;
        if (u != null) {
            u.consumeAndUpdate(r, delta);
        }
    }

}
