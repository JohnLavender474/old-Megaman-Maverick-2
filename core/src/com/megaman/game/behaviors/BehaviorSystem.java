package com.megaman.game.behaviors;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;

public class BehaviorSystem extends System {

    public BehaviorSystem() {
        super(BehaviorComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        BehaviorComponent c = e.getComponent(BehaviorComponent.class);
        for (Behavior b : c.getBehaviors()) {
            b.update(delta);
        }
    }

}
