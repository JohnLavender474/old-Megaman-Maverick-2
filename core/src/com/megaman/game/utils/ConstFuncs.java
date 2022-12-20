package com.megaman.game.utils;

import com.badlogic.gdx.math.Vector3;
import com.megaman.game.ViewVals;
import com.megaman.game.world.WorldConstVals;

public class ConstFuncs {

    public static Vector3 getCamInitPos() {
        return new Vector3(ViewVals.VIEW_WIDTH * WorldConstVals.PPM / 2f,
                ViewVals.VIEW_HEIGHT * WorldConstVals.PPM / 2f, 0f);
    }

}
