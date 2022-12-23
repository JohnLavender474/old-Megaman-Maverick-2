package com.megaman.game.shapes;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import lombok.Setter;

import java.util.PriorityQueue;

public class LineSystem extends System {

    @Setter
    private PriorityQueue<RenderableShape> gameShapesQ;

    public LineSystem() {
        super(LineComponent.class);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        LineComponent c = e.getComponent(LineComponent.class);
        for (LineHandle h : c.lineHandles) {
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
