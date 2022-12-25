package com.megaman.game.entities.sensors.impl;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.world.*;

public class DeathSensor extends Entity {

    private final Body body;
    private final Fixture deathFixture;

    public DeathSensor(MegamanGame game) {
        super(game, EntityType.SENSOR);
        body = new Body(BodyType.ABSTRACT);
        deathFixture = new Fixture(this, FixtureType.DEATH, new Rectangle());
        body.fixtures.add(deathFixture);
        putComponent(new BodyComponent(body));
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        body.bounds.set(bounds);
        ((Rectangle) deathFixture.shape).set(bounds);
    }

}
