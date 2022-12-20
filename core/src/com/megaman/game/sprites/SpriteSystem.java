package com.megaman.game.sprites;

import com.badlogic.gdx.graphics.Camera;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.Drawable;
import lombok.Setter;

import java.util.PriorityQueue;
import java.util.Queue;

public class SpriteSystem extends System {

    private Camera gameCam;
    private PriorityQueue<SpriteHandle> gameSpritesQ;

    public SpriteSystem() {
        super(SpriteComponent.class);
    }

    public void set(Camera gameCam, PriorityQueue<SpriteHandle> gameSpritesQ) {
        this.gameCam = gameCam;
        this.gameSpritesQ = gameSpritesQ;
    }

    @Override
    protected void preProcess(float delta) {
        if (gameCam == null) {
            throw new IllegalStateException("Must first set game cam");
        }
        if (gameSpritesQ == null) {
            throw new IllegalStateException("Must first set game sprites queue");
        }
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        SpriteComponent c = e.getComponent(SpriteComponent.class);
        for (SpriteHandle h : c.handles) {
            h.run();
            if (h.hidden || !h.isInCamBounds(gameCam)) {
                continue;
            }
            gameSpritesQ.add(h);
        }
    }

}
