package com.megaman.game.movement.trajectory;

import com.badlogic.gdx.math.Vector2;
import com.megaman.game.Component;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.world.Body;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TrajectoryComponent implements Component {

    public Trajectory trajectory;

    public TrajectoryComponent(Body body, String trajStr) {
        this(body, trajStr, ShapeUtils.getCenterPoint(body.bounds));
    }

    public TrajectoryComponent(Body body, String trajStr, Vector2 center) {
        trajectory = new Trajectory(body, trajStr, center);
    }

    public void reset() {
        if (trajectory == null) {
            return;
        }
        trajectory.reset();
    }

}
