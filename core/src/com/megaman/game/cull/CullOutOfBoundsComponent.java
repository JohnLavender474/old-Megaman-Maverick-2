package com.megaman.game.cull;

import com.badlogic.gdx.math.Rectangle;
import com.megaman.game.Component;
import com.megaman.game.utils.objs.Timer;

import java.util.function.Supplier;

public class CullOutOfBoundsComponent implements Component {

    public static final float DEFAULT_CULL_DUR = .5f;

    public final Timer timer;

    public Supplier<Rectangle> boundsSupplier;

    public CullOutOfBoundsComponent(Rectangle bounds) {
        this(() -> bounds);
    }

    public CullOutOfBoundsComponent(Supplier<Rectangle> boundsSupplier) {
        this(boundsSupplier, DEFAULT_CULL_DUR);
    }

    public CullOutOfBoundsComponent(Supplier<Rectangle> boundsSupplier, float cullDur) {
        this.boundsSupplier = boundsSupplier;
        timer = new Timer(cullDur);
    }

    @Override
    public void reset() {
        timer.reset();
    }

}
