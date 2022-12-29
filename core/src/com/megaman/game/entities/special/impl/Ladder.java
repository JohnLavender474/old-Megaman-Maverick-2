package com.megaman.game.entities.special.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.world.*;

public class Ladder extends Entity {

    public final Body body;

    private Fixture ladderFixture;

    public Ladder(MegamanGame game) {
        super(game, EntityType.TEST);
        body = new Body(BodyType.ABSTRACT);
        putComponent(bodyComponent());
        putComponent(shapeComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        body.bounds.set(bounds);
        ((Rectangle) ladderFixture.shape).set(bounds);
    }

    private BodyComponent bodyComponent() {
        Fixture ladderFixture = new Fixture(this, FixtureType.LADDER, new Rectangle());
        body.fixtures.add(ladderFixture);
        this.ladderFixture = ladderFixture;
        return new BodyComponent(body);
    }

    private ShapeComponent shapeComponent() {
        return new ShapeComponent(new ShapeHandle(() -> body.bounds, Color.PURPLE));
    }

}
