package com.megaman.game.movement.trajectory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.world.Body;
import com.megaman.game.world.WorldVals;

// TODO: test using vel instead of timer
public class Trajectory implements Updatable, Resettable {

    private static class TrajDef {
        private float xVel;
        private float yVel;
        private float time;
    }

    private final Body body;
    // private final Array<KeyValuePair<Vector2, Timer>> defs = new Array<>();
    private final Array<TrajDef> defs = new Array<>();

    /*
    private final Vector2 prevCenter;
    private final Vector2 currCenter;
     */

    private Vector2 startPos;
    private int index;
    private float dur;

    public Trajectory(Body body, String traj) {
        this.body = body;
        startPos = body.getPos();
        String[] tokens = traj.split(";");
        for (String token : tokens) {
            String[] vals = token.split(",");
            TrajDef def = new TrajDef();
            def.xVel = Float.parseFloat(vals[0]);
            def.yVel = Float.parseFloat(vals[1]);
            def.time = Float.parseFloat(vals[2]);
            defs.add(def);
        }
    }

    /*
    public Trajectory(Body body, String trajectory) {
        this(body, trajectory, ShapeUtils.getCenterPoint(body.bounds));
    }

    public Trajectory(Body body, String trajectory, Vector2 startCenterPoint) {
        this(body, TrajectoryParser.parse(trajectory, WorldVals.PPM), startCenterPoint);
    }

    public Trajectory(Body body, Array<KeyValuePair<Vector2, Float>> defs, Vector2 startCenterPoint) {
        this.body = body;
        prevCenter = new Vector2();
        currCenter = new Vector2();
        Vector2 temp = new Vector2(startCenterPoint);
        for (KeyValuePair<Vector2, Float> def : defs) {
            Vector2 dest = new Vector2(temp).add(def.key());
            this.defs.add(KeyValuePair.of(dest, new Timer(def.value())));
            temp.set(dest);
        }
    }
     */

    @Override
    public void update(float delta) {
        dur += delta;
        TrajDef currDef = defs.get(index);
        if (dur >= currDef.time) {
            dur = 0f;
            index++;
            if (index >= defs.size) {
                index = 0;
            }
            currDef = defs.get(index);
        }
        body.velocity.x = currDef.xVel * WorldVals.PPM;
        body.velocity.y = currDef.yVel * WorldVals.PPM;
        /*
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
        prevCenter.set(ShapeUtils.getCenterPoint(body.bounds));
        currCenter.set(center);
        body.bounds.setCenter(center);
         */
    }

    @Override
    public void reset() {
        dur = 0;
        index = 0;
        body.setPos(startPos);
        /*
        for (KeyValuePair<Vector2, Timer> def : defs) {
            def.value().reset();
        }
         */
    }

    /*
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
     */

}
