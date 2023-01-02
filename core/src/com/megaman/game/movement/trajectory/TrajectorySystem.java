package com.megaman.game.movement.trajectory;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;

public class TrajectorySystem extends System {

    public TrajectorySystem() {
        super(TrajectoryComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        Trajectory trajectory = e.getComponent(TrajectoryComponent.class).trajectory;
        if (trajectory != null) {
            trajectory.update(delta);
        }
    }

}
