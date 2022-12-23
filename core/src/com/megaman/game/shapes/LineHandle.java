package com.megaman.game.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import static com.badlogic.gdx.math.Vector2.X;
import static com.badlogic.gdx.math.Vector2.Y;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineHandle implements RenderableShape {

    public Updatable updatable;
    public Supplier<Pair<Vector2>> lineSupplier = () -> Pair.of(X, Y);
    public Supplier<ShapeType> shapeTypeSupplier = () -> ShapeType.Line;
    public Supplier<Boolean> doRenderSupplier = () -> true;
    public Supplier<Float> thicknessSupplier = () -> 1f;
    public Supplier<Integer> prioritySupplier = () -> 0;
    public Supplier<Color> colorSupplier = () -> Color.BLACK;

    public Pair<Vector2> getLine() {
        return lineSupplier.get();
    }

    public Color getColor() {
        return colorSupplier.get();
    }

    public float getThickness() {
        return thicknessSupplier.get();
    }

    public boolean doRender() {
        return doRenderSupplier.get();
    }

    @Override
    public ShapeType getShapeType() {
        return shapeTypeSupplier.get();
    }

    @Override
    public int getPriority() {
        return prioritySupplier.get();
    }

    @Override
    public void render(ShapeRenderer renderer) {
        renderer.set(getShapeType());
        renderer.setColor(getColor());
        Pair<Vector2> line = getLine();
        renderer.rectLine(line.getFirst(), line.getSecond(), getThickness());
    }

}
