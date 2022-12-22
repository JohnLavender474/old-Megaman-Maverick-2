package com.megaman.game.movement.trajectory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.utils.objs.KeyValuePair;

public class TrajectoryParser {

    public static Array<KeyValuePair<Vector2, Float>> parse(String trajectory, float scale) {
        String[] tokens = trajectory.split(";");
        return parse(tokens, scale);
    }

    public static Array<KeyValuePair<Vector2, Float>> parse(String[] tokens, float scale) {
        Array<KeyValuePair<Vector2, Float>> ans = new Array<>();
        for (String s : tokens) {
            String[] params = s.split(",");
            float x = Float.parseFloat(params[0]);
            float y = Float.parseFloat(params[1]);
            float time = Float.parseFloat(params[2]);
            Vector2 v = new Vector2(x, y).scl(scale);
            ans.add(KeyValuePair.of(v, time));
        }
        return ans;
    }

}
