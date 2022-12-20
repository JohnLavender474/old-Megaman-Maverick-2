package com.megaman.game.updatables;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.KeyValuePair;

import java.util.Iterator;

public class UpdatableSystem extends System {

    public UpdatableSystem() {
        super(UpdatableComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        UpdatableComponent c = e.getComponent(UpdatableComponent.class);
        Iterator<KeyValuePair<Updatable, UpdateQualifier>> iter = c.updatables.iterator();
        while (iter.hasNext()) {
            KeyValuePair<Updatable, UpdateQualifier> p = iter.next();
            if (p.value().doUpdate()) {
                p.key().update(delta);
            }
            if (p.value().doRemove()) {
                iter.remove();
            }
        }
    }

}
