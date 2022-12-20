package com.megaman.game;

import com.badlogic.gdx.Gdx;

public class TargetFPS {

    public static final int TARGET_FPS = 60;

    public static float getTargetDelta() {
        return 1f / (float) TARGET_FPS;
    }

    public static float getActualToTargetRatio() {
        return ((float) TARGET_FPS) / ((float) Gdx.graphics.getFramesPerSecond());
    }

}
