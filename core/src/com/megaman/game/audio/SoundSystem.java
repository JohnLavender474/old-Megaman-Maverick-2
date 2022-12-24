package com.megaman.game.audio;

import com.megaman.game.System;
import com.megaman.game.entities.Entity;

public class SoundSystem extends System {

    private final AudioManager audioMan;

    public SoundSystem(AudioManager audioMan) {
        super(SoundComponent.class);
        this.audioMan = audioMan;
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        SoundComponent c = e.getComponent(SoundComponent.class);
        while (!c.playReqs.isEmpty()) {
            audioMan.playSound(c.playReqs.poll());
        }
        while (!c.stopReqs.isEmpty()) {
            audioMan.stopSound(c.stopReqs.poll());
        }
    }

}
