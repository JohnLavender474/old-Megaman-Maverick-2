package com.megaman.game.movement.trajectory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.KeyValuePair;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Body;
import com.megaman.game.world.WorldConstVals;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Trajectory implements Updatable {

    private final Body body;
    private final Array<KeyValuePair<Vector2, Timer>> defs = new Array<>();

    private int index;

    public Trajectory(Body body, String trajectory, Vector2 startCenterPoint) {
        this(body, TrajectoryParser.parse(trajectory, WorldConstVals.PPM), startCenterPoint);
    }

    public Trajectory(Body body, Array<KeyValuePair<Vector2, Float>> defs, Vector2 startCenterPoint) {
        this.body = body;
        Vector2 temp = new Vector2(startCenterPoint);
        for (KeyValuePair<Vector2, Float> def : defs) {
            Vector2 dest = new Vector2(temp).add(def.key());
            this.defs.add(KeyValuePair.of(dest, new Timer(def.value())));
            temp.set(dest);
        }
    }

    @Override
    public void update(float delta) {
        Timer timer = getCurrentTimer();
        timer.update(delta);
        Vector2 center = UtilMethods.interpolate(getPrevDest(), getCurrentDest(), timer.getRatio());
        if (timer.isFinished()) {
            timer.reset();
            index++;
            if (index >= defs.size) {
                index = 0;
            }
        }
        body.bounds.setCenter(center);
    }

    private Vector2 getPrevDest() {
        int t = (index == 0 ? defs.size : index) - 1;
        return defs.get(t).key();
    }

    private Vector2 getCurrentDest() {
        return defs.get(index).key();
    }

    private Timer getCurrentTimer() {
        return defs.get(index).value();
    }

}
