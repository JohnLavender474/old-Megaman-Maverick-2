package com.megaman.game.cull;

import com.badlogic.gdx.math.Rectangle;
import com.megaman.game.Component;
import com.megaman.game.utils.objs.Timer;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@NoArgsConstructor
public class CullOutOfBoundsComponent implements Component {

    public static final float CULL_DUR = .5f;

    public Timer timer = new Timer(CULL_DUR);
    public Supplier<Rectangle> boundsSupplier;

    public CullOutOfBoundsComponent(Supplier<Rectangle> boundsSupplier) {
        this.boundsSupplier = boundsSupplier;
    }

}
