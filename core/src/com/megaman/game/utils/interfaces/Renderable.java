package com.megaman.game.utils.interfaces;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Renderable extends Comparable<Renderable> {

    void render(SpriteBatch batch);

    int getPriority();

    @Override
    default int compareTo(Renderable o) {
        return getPriority() - o.getPriority();
    }

}
