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
        if (c.rotatingLine != null) {
            c.rotatingLine.update(delta);
        }
        if (c.updatable != null) {
            c.updatable.update(delta);
        }
    }

}
