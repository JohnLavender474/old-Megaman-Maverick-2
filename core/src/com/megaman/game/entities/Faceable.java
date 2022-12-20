package com.megaman.game.entities;

public interface Faceable {

    Facing getFacing();

    void setFacing(Facing facing);

    default boolean is(Facing facing) {
        return getFacing() == facing;
    }

}
