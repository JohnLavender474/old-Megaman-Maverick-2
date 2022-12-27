package com.megaman.game.utils;

import com.badlogic.gdx.math.Vector3;
import com.megaman.game.ViewVals;
import com.megaman.game.world.WorldVals;

public class ConstFuncs {

    public static Vector3 getCamInitPos() {
        Vector3 v = new Vector3();
        v.x = ViewVals.VIEW_WIDTH * WorldVals.PPM / 2f;
        v.y = ViewVals.VIEW_HEIGHT * WorldVals.PPM / 2f;
        return v;
    }

}
