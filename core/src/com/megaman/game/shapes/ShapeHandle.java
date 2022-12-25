package com.megaman.game.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.*;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Pair;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

import static com.badlogic.gdx.graphics.Color.RED;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;

@Setter
@NoArgsConstructor
public class ShapeHandle implements RenderableShape {

    public Updatable updatable;
    public Supplier<Color> colorSupplier = () -> RED;
    public Supplier<Integer> prioritySupplier = () -> 0;
    public Supplier<Shape2D> shapeSupplier = () -> null;
    public Supplier<Boolean> doRenderSupplier = () -> true;
    public Supplier<ShapeType> shapeTypeSupplier = () -> Line;

    public ShapeHandle(Shape2D shape) {
        this(() -> shape);
    }

    public ShapeHandle(Shape2D shape, Color color) {
        this(shape, () -> color);
    }

    public ShapeHandle(Shape2D shape, Supplier<Color> colorSupplier) {
        this(() -> shape, colorSupplier);
    }

    public ShapeHandle(Supplier<Shape2D> shapeSupplier) {
        this(shapeSupplier, () -> RED);
    }

    public ShapeHandle(Supplier<Shape2D> shapeSupplier, Supplier<Color> colorSupplier) {
        this.shapeSupplier = shapeSupplier;
        this.colorSupplier = colorSupplier;
    }

    public Color getColor() {
        return colorSupplier.get();
    }

    public Shape2D getShape() {
        return shapeSupplier.get();
    }

    public boolean doRender() {
        return doRenderSupplier.get();
    }

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
        Shape2D s = getShape();
        if (s instanceof Rectangle rectangle) {
            renderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        } else if (s instanceof Circle circle) {
            renderer.circle(circle.x, circle.y, circle.radius);
        } else if (s instanceof Polyline line) {
            Pair<Vector2> l = ShapeUtils.polylineToPointPair(line);
            renderer.line(l.getFirst(), l.getSecond());
        }
    }

}
