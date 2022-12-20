package com.megaman.game.controllers;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;

import java.util.Map;

public class ControllerSystem extends System {

    private final ControllerManager ctrlManager;

    public ControllerSystem(ControllerManager ctrlManager) {
        super(ControllerComponent.class);
        this.ctrlManager = ctrlManager;
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        ControllerComponent c = e.getComponent(ControllerComponent.class);
        for (Map.Entry<ControllerBtn, ControllerAdapter> entry : c.ctrlAdapters.entrySet()) {
            if (ctrlManager.isJustPressed(entry.getKey())) {
                entry.getValue().onJustPressed();
            } else if (ctrlManager.isPressed(entry.getKey())) {
                entry.getValue().onPressContinued(delta);
            } else if (ctrlManager.isJustReleased(entry.getKey())) {
                entry.getValue().onJustReleased();
            }
        }
    }

}
