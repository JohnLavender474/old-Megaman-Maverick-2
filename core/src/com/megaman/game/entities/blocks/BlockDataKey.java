package com.megaman.game.entities.blocks;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum BlockDataKey {

    RESIST_ON("resist_on"),
    GRAVITY_ON("gravity_on"),
    FRICTION_X("friction_x"),
    FRICTION_Y("friction_y"),
    WALL_SLIDE_ON("wall_slide_on");

    public final String key;

}
