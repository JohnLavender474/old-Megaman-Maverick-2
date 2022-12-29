package com.megaman.game.screens.levels.spawns;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.GameEngine;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class Spawn implements Updatable {

    protected final GameEngine engine;
    protected final Rectangle bounds;
    protected final ObjectMap<String, Object> data;
    protected final Supplier<Entity> entitySupplier;

    protected Entity entity;

    public boolean doRemove() {
        return false;
    }

    protected void spawnEntity() {
        entity = entitySupplier.get();
        engine.spawn(entity, bounds, data);
    }

}
