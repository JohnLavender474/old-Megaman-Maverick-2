package com.megaman.game.world;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.graph.Node;
import com.megaman.game.utils.interfaces.Resettable;

public class WorldNode extends Node implements Resettable {

    public Array<Body> bodies;
    public Array<Fixture> fixtures;

    public WorldNode(int x, int y) {
        super(x, y);
        reset();
    }

    @Override
    public void reset() {
        bodies = new Array<>(120);
        fixtures = new Array<>(120);
    }

}
