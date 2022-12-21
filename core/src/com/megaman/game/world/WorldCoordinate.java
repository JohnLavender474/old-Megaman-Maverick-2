package com.megaman.game.world;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WorldCoordinate {

    public final int x;
    public final int y;

    @Override
    public boolean equals(Object o) {
        return o instanceof WorldCoordinate c && x == c.x && y == c.y;
    }

    @Override
    public int hashCode() {
        int hash = 49;
        hash += 7 * x;
        hash += 7 * y;
        return hash;
    }

}
