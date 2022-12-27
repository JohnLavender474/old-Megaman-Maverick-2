package com.megaman.game.updatables;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.KeyValuePair;

import java.util.function.Supplier;

public class UpdatableSystem extends System {

    public UpdatableSystem() {
        super(UpdatableComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        UpdatableComponent c = e.getComponent(UpdatableComponent.class);
        for (KeyValuePair<Updatable, Supplier<Boolean>> p : c.updatables) {
            if (p.value().get()) {
                p.key().update(delta);
            }
        }
    }

}
