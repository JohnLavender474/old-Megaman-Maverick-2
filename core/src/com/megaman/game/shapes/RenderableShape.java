package com.megaman.game.shapes;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface RenderableShape extends Comparable<RenderableShape> {

    void render(ShapeRenderer renderer);

    int getPriority();

    ShapeRenderer.ShapeType getShapeType();

    default int compareTo(RenderableShape r) {
        return Integer.compare(getPriority(), r.getPriority());
    }

}
