package com.megaman.game.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Shape2D;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

import static com.badlogic.gdx.graphics.Color.RED;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line;

@Getter
@Setter
@NoArgsConstructor
public class ShapeHandle implements Comparable<ShapeHandle> {

    private Updatable updatable = null;
    private Supplier<Color> colorSupplier = () -> RED;
    private Supplier<Integer> prioritySupplier = () -> 0;
    private Supplier<Shape2D> shapeSupplier = () -> null;
    private Supplier<Boolean> doRenderSupplier = () -> true;
    private Supplier<ShapeType> shapeTypeSupplier = () -> Line;

    public ShapeHandle(Shape2D shape) {
        setShapeSupplier(() -> shape);
    }

    public void copyOf(ShapeHandle shapeHandle) {
        setUpdatable(shapeHandle.getUpdatable());
        setColorSupplier(shapeHandle.getColorSupplier());
        setShapeSupplier(shapeHandle.getShapeSupplier());
        setDoRenderSupplier(shapeHandle.getDoRenderSupplier());
        setShapeTypeSupplier(shapeHandle.getShapeTypeSupplier());
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

    public int getPriority() {
        return prioritySupplier.get();
    }

    @Override
    public int compareTo(ShapeHandle o) {
        return Integer.compare(getPriority(), o.getPriority());
    }

}
