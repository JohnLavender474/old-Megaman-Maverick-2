package com.megaman.game.movement.trajectory;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;

public class TrajectorySystem extends System {

    public TrajectorySystem() {
        super(TrajectoryComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        TrajectoryComponent c = e.getComponent(TrajectoryComponent.class);
        if (c.doUpdate) {
            c.trajectory.update(delta);
        }
    }

}
