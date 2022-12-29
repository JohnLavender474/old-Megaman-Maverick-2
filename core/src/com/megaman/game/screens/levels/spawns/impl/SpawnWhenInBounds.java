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
    private boolean respawnable;

    public SpawnWhenInBounds(GameEngine engine, Camera gameCam, Rectangle bounds, ObjectMap<String, Object> data,
                             Supplier<Entity> entitySupplier) {
        this(engine, gameCam, bounds, data, entitySupplier, true);
    }

    public SpawnWhenInBounds(GameEngine engine, Camera gameCam, Rectangle bounds, ObjectMap<String, Object> data,
                             Supplier<Entity> entitySupplier, boolean respawnable) {
        super(engine, bounds, data, entitySupplier);
        this.respawnable = respawnable;
        this.gameCam = gameCam;
    }

    @Override
    public void update(float delta) {
        if (entity != null && entity.dead && respawnable) {
            entity = null;
        }
        boolean wasInCamBounds = inCamBounds;
        inCamBounds = gameCam.frustum.boundsInFrustum(ShapeUtils.rectToBBox(bounds));
        if (entity == null && !wasInCamBounds && inCamBounds) {
            spawnEntity();
        }
    }

    @Override
    public boolean doRemove() {
        return !respawnable && entity != null && entity.dead;
    }

}
