package com.megaman.game.controllers;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;

import java.util.Map;

public class ControllerSystem extends System {

    private final ControllerManager ctrlMan;

    public ControllerSystem(ControllerManager ctrlMan) {
        super(ControllerComponent.class);
        this.ctrlMan = ctrlMan;
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        ControllerComponent c = e.getComponent(ControllerComponent.class);
        for (Map.Entry<CtrlBtn, ControllerActuator> entry : c.ctrlAdapters.entrySet()) {
            if (ctrlMan.isJustPressed(entry.getKey())) {
                entry.getValue().onJustPressed();
            } else if (ctrlMan.isPressed(entry.getKey())) {
                entry.getValue().onPressContinued(delta);
            } else if (ctrlMan.isJustReleased(entry.getKey())) {
                entry.getValue().onJustReleased();
            } else {
                entry.getValue().onReleaseContinued();
            }
        }
    }

}
