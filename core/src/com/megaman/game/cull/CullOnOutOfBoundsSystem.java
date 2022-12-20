package com.megaman.game.cull;

import com.badlogic.gdx.graphics.Camera;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.UtilMethods;
import lombok.Setter;

public class CullOnOutOfBoundsSystem extends System {

    @Setter
    private Camera gameCam;

    public CullOnOutOfBoundsSystem() {
        super(CullOutOfBoundsComponent.class);
    }

    @Override
    protected void preProcess(float delta) {
        if (gameCam == null) {
            throw new IllegalStateException("Camera must first be set");
        }
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        CullOutOfBoundsComponent c = e.getComponent(CullOutOfBoundsComponent.class);
        if (gameCam.frustum.boundsInFrustum(UtilMethods.rectToBBox(c.boundsSupplier.get()))) {
            c.timer.reset();
            return;
        }
        c.timer.update(delta);
        if (c.timer.isFinished()) {
            e.dead = true;
        }
    }

}
