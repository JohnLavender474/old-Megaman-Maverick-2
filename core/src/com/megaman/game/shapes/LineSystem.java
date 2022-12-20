package com.megaman.game.shapes;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.objs.Pair;
import lombok.Setter;

import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;

public class LineSystem extends System {

    private final ShapeRenderer shapeRenderer;

    @Setter
    private Camera gameCam;

    public LineSystem(ShapeRenderer shapeRenderer) {
        super(LineComponent.class);
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    protected void preProcess(float delta) {
        if (gameCam == null) {
            throw new IllegalStateException("Must first set game gameCam");
        }
        shapeRenderer.setProjectionMatrix(gameCam.combined);
        shapeRenderer.begin(Line);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        LineComponent c = e.getComponent(LineComponent.class);
        for (LineHandle h : c.lineHandles) {
            if (!h.doRender()) {
                continue;
            }
            shapeRenderer.setColor(h.getColor());
            shapeRenderer.set(h.getShapeType());
            float thickness = h.getThickness();
            Pair<Vector2> line = h.getLine();
            shapeRenderer.rectLine(line.getFirst(), line.getSecond(), thickness);
        }
    }

    @Override
    protected void postProcess(float delta) {
        shapeRenderer.end();
    }

}
