package com.megaman.game.shapes;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Pair;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class ShapeSystem extends System {

    private final Map<ShapeType, Queue<ShapeHandle>> shapeHandles = new EnumMap<>(ShapeType.class);
    private final ShapeRenderer shapeRenderer;

    @Setter
    private Camera gameCam;

    public ShapeSystem(ShapeRenderer shapeRenderer) {
        super(ShapeComponent.class);
        this.shapeRenderer = shapeRenderer;
        for (ShapeType shapeType : ShapeType.values()) {
            shapeHandles.put(shapeType, new PriorityQueue<>());
        }
    }

    @Override
    protected void preProcess(float delta) {
        if (gameCam == null) {
            throw new IllegalStateException("Must first set game gameCam");
        }
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        ShapeComponent c = e.getComponent(ShapeComponent.class);
        for (ShapeHandle h : c.shapeHandles) {
            if (!h.doRender()) {
                continue;
            }
            shapeHandles.get(h.getShapeType()).add(h);
        }
    }

    @Override
    protected void postProcess(float delta) {
        shapeRenderer.setProjectionMatrix(gameCam.combined);
        if (shapeRenderer.isDrawing()) {
            shapeRenderer.end();
        }
        for (Map.Entry<ShapeType, Queue<ShapeHandle>> entry : shapeHandles.entrySet()) {
            shapeRenderer.begin(entry.getKey());
            Queue<ShapeHandle> q = entry.getValue();
            while (!q.isEmpty()) {
                ShapeHandle s = q.poll();
                Updatable updatable = s.getUpdatable();
                if (updatable != null) {
                    updatable.update(delta);
                }
                Shape2D shape = s.getShape();
                if (shape == null) {
                    return;
                }
                shapeRenderer.set(s.getShapeType());
                shapeRenderer.setColor(s.getColor());
                if (shape instanceof Rectangle rectangle) {
                    shapeRenderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                } else if (shape instanceof Circle circle) {
                    shapeRenderer.circle(circle.x, circle.y, circle.radius);
                } else if (shape instanceof Polyline line) {
                    Pair<Vector2> l = ShapeUtils.polylineToPointPair(line);
                    shapeRenderer.line(l.getFirst(), l.getSecond());
                }
            }
            shapeRenderer.end();
        }
    }

}
