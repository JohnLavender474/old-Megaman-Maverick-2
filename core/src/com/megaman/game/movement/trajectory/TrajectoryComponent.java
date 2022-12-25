package com.megaman.game.movement.trajectory;

import com.badlogic.gdx.math.Vector2;
import com.megaman.game.Component;
import com.megaman.game.world.Body;

public class TrajectoryComponent implements Component {

    public Trajectory trajectory;
    public boolean doUpdate;

    public TrajectoryComponent() {
        doUpdate = true;
    }

    public TrajectoryComponent(Body body, String trajStr, Vector2 center) {
        this(body, trajStr, center, true);
    }

    public TrajectoryComponent(Body body, String trajStr, Vector2 center, boolean doUpdate) {
        this.doUpdate = doUpdate;
        trajectory = new Trajectory(body, trajStr, center);
    }

    public void reset() {
        if (trajectory != null) {
            trajectory.reset();
        }
    }

}
