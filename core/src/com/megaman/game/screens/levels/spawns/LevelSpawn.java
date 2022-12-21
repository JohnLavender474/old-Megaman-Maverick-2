package com.megaman.game.screens.levels.spawns;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.GameEngine;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.ShapeUtils;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class LevelSpawn {

    private final Rectangle bounds;
    private final Supplier<Entity> entitySupplier;
    private final Supplier<ObjectMap<String, Object>> dataSupplier;

    private Entity entity;
    private boolean inCamBounds;

    public void update(GameEngine engine, Camera cam) {
        if (entity != null && entity.dead) {
            entity = null;
        }
        boolean wasInCamBounds = inCamBounds;
        inCamBounds = cam.frustum.boundsInFrustum(ShapeUtils.rectToBBox(bounds));
        if (entity == null && !wasInCamBounds && inCamBounds) {
            entity = entitySupplier.get();
            engine.spawnEntity(entity, bounds, dataSupplier.get());
        }
    }

}
