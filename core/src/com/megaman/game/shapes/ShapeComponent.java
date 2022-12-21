package com.megaman.game.shapes;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ShapeComponent implements Component {

    public Array<ShapeHandle> shapeHandles = new Array<>();

    public ShapeComponent(ShapeHandle... shapeHandles) {
        this.shapeHandles.addAll(shapeHandles);
    }

    public ShapeComponent(Iterable<ShapeHandle> shapeHandles) {
        for (ShapeHandle h : shapeHandles) {
            this.shapeHandles.add(h);
        }
    }

}
