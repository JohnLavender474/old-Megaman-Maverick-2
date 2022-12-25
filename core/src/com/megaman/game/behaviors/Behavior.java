package com.megaman.game.behaviors;

import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class Behavior implements Updatable, Resettable {

    private boolean runningNow;

    protected abstract boolean evaluate(float delta);

    protected abstract void init();

    protected abstract void act(float delta);

    protected abstract void end();

    @Override
    public final void update(float delta) {
        boolean runningPrior = runningNow;
        runningNow = evaluate(delta);
        if (runningNow && !runningPrior) {
            init();
        }
        if (runningNow) {
            act(delta);
        }
        if (!runningNow && runningPrior) {
            end();
        }
    }

    @Override
    public void reset() {
        runningNow = false;
    }

}
