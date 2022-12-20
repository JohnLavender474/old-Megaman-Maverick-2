package com.megaman.game.movement.rotatingline;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.UpdatableConsumer;

public class RotatingLineSystem extends System {

    public RotatingLineSystem() {
        super(RotatingLineComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        RotatingLineComponent rlc = e.getComponent(RotatingLineComponent.class);
        RotatingLine rl = rlc.rotatingLine;
        if (rl == null) {
            return;
        }
        rl.update(delta);
        UpdatableConsumer<RotatingLine> uc = rlc.updatableConsumer;
        if (uc == null) {
            return;
        }
        uc.consumeAndUpdate(rl, delta);
    }

}
