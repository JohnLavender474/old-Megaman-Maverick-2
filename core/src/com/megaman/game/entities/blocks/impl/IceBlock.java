package com.megaman.game.entities.blocks.impl;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.blocks.Block;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;

public class IceBlock extends Block {

    private Fixture iceFixture;

    public IceBlock(MegamanGame game) {
        super(game, true);
        Fixture iceFixture = new Fixture(this, FixtureType.ICE, new Rectangle());
        body.fixtures.add(iceFixture);
        this.iceFixture = iceFixture;
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        super.init(bounds, data);
        ((Rectangle) iceFixture.shape).set(bounds);
    }

}
