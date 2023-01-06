package com.megaman.game.screens.levels.camera;

import com.badlogic.gdx.math.Vector2;

public interface LevelCamFocusable {

    Vector2 getFocus();

    default Vector2 getTransPoint() {
        return getFocus();
    }

}
