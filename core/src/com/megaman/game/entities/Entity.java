package com.megaman.game.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.Component;

public class Entity {

    public final ObjectMap<Class<? extends Component>, Component> components = new ObjectMap<>();
    public final Array<Runnable> runOnDeath = new Array<>();
    public final EntityType entityType;
    public final MegamanGame game;

    public boolean dead = true;

    public Entity(MegamanGame game, EntityType entityType) {
        this.game = game;
        this.entityType = entityType;
    }

    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        throw new IllegalStateException("Init method not implemented");
    }

    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        throw new IllegalStateException("Init method not implemented");
    }

    public void addComponent(Component c) {
        components.put(c.getClass(), c);
    }

    public <C extends Component> C getComponent(Class<C> cClass) {
        return cClass.cast(components.get(cClass));
    }

    public boolean hasComponent(Class<? extends Component> cClass) {
        return components.containsKey(cClass);
    }

}
