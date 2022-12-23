package com.megaman.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.entities.Entity;
import com.megaman.game.shapes.ShapeUtils;

public class Fixture {

    public final Entity entity;
    public final FixtureType fixtureType;

    public Shape2D shape;
    public boolean active = true;
    public Vector2 offset = new Vector2();
    public ObjectMap<String, Object> userData = new ObjectMap<>();

    public Fixture(Entity entity, FixtureType fixtureType) {
        this(entity, fixtureType, new Rectangle());
    }

    public Fixture(Entity entity, FixtureType fixtureType, Shape2D shape) {
        this.shape = shape;
        this.entity = entity;
        this.fixtureType = fixtureType;
    }

    public boolean overlaps(Fixture fixture) {
        return ShapeUtils.overlaps(shape, fixture.shape);
    }

    public void putUserData(String key, Object o) {
        userData.put(key, o);
    }

    public boolean hasUserData(String key) {
        return userData.containsKey(key);
    }

    public <T> T getUserData(String key, Class<T> tClass) {
        return tClass.cast(userData.get(key));
    }

    public <T> T getUserDataOrDefault(String key, Class<T> tClass, T def) {
        if (!hasUserData(key)) {
            return def;
        }
        return getUserData(key, tClass);
    }

}
