package com.megaman.game.movement.trajectory;

import com.megaman.game.Component;
import com.megaman.game.world.Body;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TrajectoryComponent implements Component {

    public Trajectory trajectory;

    public TrajectoryComponent(Body body, String trajStr) {
        trajectory = new Trajectory(body, trajStr);
    }

    /*
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
     */

    public void reset() {
        if (trajectory != null) {
            trajectory.reset();
        }
    }

}
