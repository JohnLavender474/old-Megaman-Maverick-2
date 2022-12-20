package com.megaman.game.animations;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;

public class AnimationSystem extends System {

    public AnimationSystem() {
        super(AnimationComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        AnimationComponent c = e.getComponent(AnimationComponent.class);
        c.animators.forEach(a -> a.update(delta));
    }

}
