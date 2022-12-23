package com.megaman.game.shapes;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import lombok.Setter;

import java.util.PriorityQueue;

public class ShapeSystem extends System {

    @Setter
    private PriorityQueue<RenderableShape> gameShapesQ;

    public ShapeSystem() {
        super(ShapeComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        ShapeComponent c = e.getComponent(ShapeComponent.class);
        for (ShapeHandle h : c.shapeHandles) {
            if (!h.doRender()) {
                continue;
            }
            if (h.updatable != null) {
                h.updatable.update(delta);
            }
            gameShapesQ.add(h);
        }
    }

}
