package com.megaman.game.sprites;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import lombok.Setter;

import java.util.PriorityQueue;
import java.util.Queue;

public class SpriteSystem extends System {

    private final SpriteBatch batch;
    private final Queue<SpriteHandle> q = new PriorityQueue<>();

    @Setter
    private Camera gameCam;

    public SpriteSystem(SpriteBatch batch) {
        super(SpriteComponent.class);
        this.batch = batch;
    }

    @Override
    protected void preProcess(float delta) {
        if (gameCam == null) {
            throw new IllegalStateException("Must first set game gameCam");
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
            q.add(h);
        }
    }

    @Override
    protected void postProcess(float delta) {
        batch.setProjectionMatrix(gameCam.combined);
        batch.begin();
        while (!q.isEmpty()) {
            q.poll().draw(batch);
        }
        batch.end();
    }

}
