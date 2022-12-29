package com.megaman.game.world;

import com.badlogic.gdx.math.Rectangle;

public interface SpecialCollisionHandler {

    boolean handleSpecial(Body dynamicBody, Body staticBody, Rectangle overlap);

}
