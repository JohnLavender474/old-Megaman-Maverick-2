package com.megaman.game.movement.trajectory;

import com.megaman.game.Component;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.world.Body;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TrajectoryComponent implements Component {

    public Trajectory trajectory;

    public TrajectoryComponent(Body body, String trajStr) {
        trajectory = new Trajectory(body, trajStr, ShapeUtils.getCenterPoint(body.bounds));
    }

    public void reset() {
        if (trajectory == null) {
            return;
        }
        trajectory.reset();
    }

}
