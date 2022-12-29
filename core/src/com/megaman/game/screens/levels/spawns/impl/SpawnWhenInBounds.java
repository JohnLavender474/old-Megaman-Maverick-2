package com.megaman.game.screens.levels.spawns.impl;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.GameEngine;
import com.megaman.game.entities.Entity;
import com.megaman.game.screens.levels.spawns.Spawn;
import com.megaman.game.shapes.ShapeUtils;

import java.util.function.Supplier;

public class SpawnWhenInBounds extends Spawn {

    private final Camera gameCam;

    private boolean inCamBounds;

    public SpawnWhenInBounds(GameEngine engine, Camera gameCam, Rectangle bounds, ObjectMap<String, Object> data,
                             Supplier<Entity> entitySupplier) {
        super(engine, bounds, data, entitySupplier);
        this.gameCam = gameCam;
    }

    @Override
    public void update(float delta) {
        if (entity != null && entity.dead) {
            entity = null;
        }
        boolean wasInCamBounds = inCamBounds;
        inCamBounds = gameCam.frustum.boundsInFrustum(ShapeUtils.rectToBBox(bounds));
        if (entity == null && !wasInCamBounds && inCamBounds) {
            entity = entitySupplier.get();
            engine.spawn(entity, bounds, data);
        }
    }

}
