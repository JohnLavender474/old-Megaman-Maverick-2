package com.megaman.game.utils.interfaces;

import com.badlogic.gdx.math.Vector2;

public interface Positional {

    Vector2 getPosition();

    void setPosition(float x, float y);

    default void setPosition(Vector2 pos) {
        setPosition(pos.x, pos.y);
    }

}
