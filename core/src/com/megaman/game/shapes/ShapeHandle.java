package com.megaman.game.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.*;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Pair;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

import static com.badlogic.gdx.graphics.Color.RED;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;

@Getter
@Setter
@NoArgsConstructor
public class ShapeHandle implements RenderableShape {

    public Updatable updatable = null;
    public Supplier<Color> colorSupplier = () -> RED;
    public Supplier<Shape2D> shapeSupplier = () -> null;
    public Supplier<Boolean> doRenderSupplier = () -> true;
    public Supplier<ShapeType> shapeTypeSupplier = () -> Line;

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
    public void render(ShapeRenderer renderer) {
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
