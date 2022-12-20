package com.megaman.game.shapes;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.Setter;

import java.util.Map;
import java.util.Queue;

public class ShapeSystem extends System {

    @Setter
    private Map<ShapeRenderer.ShapeType, Queue<RenderableShape>> shapeRenderQs;

    public ShapeSystem() {
        super(ShapeComponent.class);
    }

    @Override
    protected void preProcess(float delta) {
        if (shapeRenderQs == null) {
            throw new IllegalStateException("Must first set render shape queue");
        }
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        ShapeComponent c = e.getComponent(ShapeComponent.class);
        for (ShapeHandle h : c.shapeHandles) {
            if (!h.doRender()) {
                continue;
            }
            Updatable u = h.getUpdatable();
            if (u != null) {
                u.update(delta);
            }
            Queue<RenderableShape> q = shapeRenderQs.get(h.getShapeType());
            if (q == null) {
                continue;
            }
            q.add(h);
        }
    }

}
