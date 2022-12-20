package com.megaman.game.behaviors;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.Updatable;

public class BehaviorSystem extends System {

    public BehaviorSystem() {
        super(BehaviorComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        BehaviorComponent behaviorComponent = e.getComponent(BehaviorComponent.class);
        Updatable preProcess = behaviorComponent.preProcess;
        if (preProcess != null) {
            preProcess.update(delta);
        }
        for (Behavior behavior : behaviorComponent.behaviors) {
            behavior.update(delta);
        }
        Updatable postProcess = behaviorComponent.postProcess;
        if (postProcess != null) {
            postProcess.update(delta);
        }
    }

}
