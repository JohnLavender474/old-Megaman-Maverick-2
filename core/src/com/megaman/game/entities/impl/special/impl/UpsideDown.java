package com.megaman.game.entities.impl.special.impl;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.world.*;

public class UpsideDown extends Entity {

    private final Body body;
    private final Fixture upsideDownFixture;

    public UpsideDown(MegamanGame game) {
        super(game, EntityType.SPECIAL);
        body = new Body(BodyType.ABSTRACT);
        upsideDownFixture = new Fixture(this, FixtureType.UPSIDE_DOWN, new Rectangle());
        body.add(upsideDownFixture);

        putComponent(new BodyComponent(body));
        putComponent(new CullOutOfBoundsComponent(() -> body.bounds));
        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(upsideDownFixture.shape));
        }
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        body.bounds.set(bounds);
        ((Rectangle) upsideDownFixture.shape).set(bounds);
    }

}
