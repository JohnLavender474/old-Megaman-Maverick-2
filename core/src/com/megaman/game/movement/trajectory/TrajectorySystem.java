package com.megaman.game.movement.trajectory;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;

public class TrajectorySystem extends System {

    public TrajectorySystem() {
        super(TrajectoryComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        TrajectoryComponent tc = e.getComponent(TrajectoryComponent.class);
        for (Trajectory t : tc.trajectories) {
            t.update(delta);
        }
    }

}
