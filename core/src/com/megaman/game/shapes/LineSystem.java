package com.megaman.game.shapes;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import lombok.Setter;

import java.util.Map;
import java.util.Queue;

public class LineSystem extends System {

    @Setter
    private Map<ShapeRenderer.ShapeType, Queue<RenderableShape>> shapeRenderQs;

    public LineSystem() {
        super(LineComponent.class);
    }

    @Override
    protected void preProcess(float delta) {
        if (shapeRenderQs == null) {
            throw new IllegalStateException("Must first set shape render queues");
        }
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        LineComponent c = e.getComponent(LineComponent.class);
        for (LineHandle h : c.lineHandles) {
            if (!h.doRender()) {
                continue;
            }
            Queue<RenderableShape> q = shapeRenderQs.get(h.getShapeType());
            q.add(h);
        }
    }

}
