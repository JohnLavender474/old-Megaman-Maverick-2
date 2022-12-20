package com.megaman.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.entities.Entity;

import java.util.EnumSet;
import java.util.Set;

public class Fixture {

    public final Entity entity;
    public final FixtureType fixtureType;

    public Rectangle bounds;
    public boolean active = true;
    public Vector2 offset = new Vector2();
    public ObjectMap<String, Object> userData = new ObjectMap<>();

    public Fixture(Entity entity, FixtureType fixtureType) {
        this(entity, fixtureType, Vector2.Zero);
    }

    public Fixture(Entity entity, FixtureType fixtureType, float squareDimension) {
        this(entity, fixtureType, new Vector2(squareDimension, squareDimension));
    }

    public Fixture(Entity entity, FixtureType fixtureType, Vector2 size) {
        this(entity, fixtureType, new Rectangle(0f, 0f, size.x, size.y));
    }

    public Fixture(Entity entity, FixtureType fixtureType, Rectangle bounds) {
        this.entity = entity;
        this.fixtureType = fixtureType;
        this.bounds = new Rectangle(bounds);
    }

    public boolean overlaps(Fixture fixture) {
        return bounds.overlaps(fixture.bounds);
    }

}
