package com.megaman.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.shapes.ShapeUtils;

public class Fixture {

    public final Entity entity;
    public final FixtureType fixtureType;

    public Shape2D shape;
    public Vector2 offset;
    public boolean active;
    public boolean attached;
    public ObjectMap<String, Object> userData;

    public Fixture(Entity entity, FixtureType fixtureType) {
        this(entity, fixtureType, new Rectangle());
    }

    public Fixture(Entity entity, FixtureType fixtureType, Body body) {
        this(entity, fixtureType, body, true);
    }

    public Fixture(Entity entity, FixtureType fixtureType, Body body, boolean attached) {
        this(entity, fixtureType, body.bounds, attached);
    }

    public Fixture(Entity entity, FixtureType fixtureType, Shape2D shape) {
        this(entity, fixtureType, shape, true);
    }

    public Fixture(Entity entity, FixtureType fixtureType, Shape2D shape, boolean attached) {
        this.shape = shape;
        this.entity = entity;
        this.fixtureType = fixtureType;
        this.attached = attached;
        active = true;
        offset = new Vector2();
        userData = new ObjectMap<>();
    }

    public boolean overlaps(Fixture fixture) {
        return ShapeUtils.overlaps(shape, fixture.shape);
    }

    public boolean hasUserData(String key) {
        return userData.containsKey(key);
    }

    public void putUserData(String key, Object o) {
        userData.put(key, o);
    }

    public <T> T getUserData(String key, Class<T> tClass) {
        return tClass.cast(userData.get(key));
    }

    public void removeUserData(String key) {
        userData.remove(key);
    }

}
