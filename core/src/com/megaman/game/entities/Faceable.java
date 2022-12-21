package com.megaman.game.entities;

public interface Faceable {

    Facing getFacing();

    void setFacing(Facing facing);

    default void swapFacing() {
        setFacing(getFacing() == Facing.LEFT ? Facing.RIGHT : Facing.LEFT);
    }

    default boolean is(Facing facing) {
        return getFacing() == facing;
    }

}
