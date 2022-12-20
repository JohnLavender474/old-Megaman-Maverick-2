package com.megaman.game.audio;

import com.badlogic.gdx.audio.Sound;
import com.megaman.game.System;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.Updatable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;

public class SoundSystem extends System {

    private final AudioManager audioManager;
    private final AssetsManager assetsManager;
    private final Map<SoundAsset, Sound> loopingSounds = new EnumMap<>(SoundAsset.class);

    public boolean stopAllLoopingSounds;

    public SoundSystem(AssetsManager assetsManager, AudioManager audioManager) {
        super(SoundComponent.class);
        this.assetsManager = assetsManager;
        this.audioManager = audioManager;
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        SoundComponent soundComponent = e.getComponent(SoundComponent.class);
        Updatable updatable = soundComponent.updatable;
        if (updatable != null) {
            updatable.update(delta);
        }
        Queue<SoundRequest> soundRequests = soundComponent.sReqs;
        while (!soundRequests.isEmpty()) {
            SoundRequest soundRequest = soundRequests.poll();
            Sound sound = assetsManager.getAsset(soundRequest.sAss.getSrc(), Sound.class);
            if (soundRequest.loop && !loopingSounds.containsKey(soundRequest.sAss)) {
                audioManager.playSound(sound, true);
                loopingSounds.put(soundRequest.sAss, sound);
            } else {
                audioManager.playSound(sound, false);
            }
        }
        Queue<SoundAsset> stopLoopingSoundRequests = soundComponent.loopsToStop;
        while (!stopLoopingSoundRequests.isEmpty()) {
            SoundAsset stopLoopingSoundRequest = stopLoopingSoundRequests.poll();
            Sound sound = loopingSounds.remove(stopLoopingSoundRequest);
            if (sound != null) {
                sound.stop();
            }
        }
        if (stopAllLoopingSounds) {
            loopingSounds.values().forEach(Sound::stop);
            loopingSounds.clear();
            stopAllLoopingSounds = false;
        }
    }

}
