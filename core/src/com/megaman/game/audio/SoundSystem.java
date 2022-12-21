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

    private final AssetsManager assMan;
    private final AudioManager audioMan;
    private final Map<SoundAsset, Sound> loops;

    private boolean reqStopLoops;

    public SoundSystem(AssetsManager assMan, AudioManager audioMan) {
        super(SoundComponent.class);
        this.assMan = assMan;
        this.audioMan = audioMan;
        this.loops = new EnumMap<>(SoundAsset.class);
    }

    public void reqStopAllLoops() {
        if (updating) {
            reqStopLoops = true;
            return;
        }
        stopAllLoops();
    }

    private void stopAllLoops() {
        for (Sound s : loops.values()) {
            s.stop();
        }
        loops.clear();
        reqStopLoops = false;
    }

    @Override
    protected void preProcess(float delta) {
        if (reqStopLoops) {
            stopAllLoops();
        }
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        SoundComponent c = e.getComponent(SoundComponent.class);
        Updatable u = c.updatable;
        if (u != null) {
            u.update(delta);
        }
        Queue<SoundRequest> reqQ = c.sReqs;
        while (!reqQ.isEmpty()) {
            SoundRequest req = reqQ.poll();
            Sound s = assMan.getAsset(req.sAss.getSrc(), Sound.class);
            if (req.loop && !loops.containsKey(req.sAss)) {
                audioMan.playSound(s, true);
                loops.put(req.sAss, s);
            } else {
                audioMan.playSound(s, false);
            }
        }
        while (!c.loopsToStop.isEmpty()) {
            SoundAsset stopLoopReq = c.loopsToStop.poll();
            Sound s = loops.remove(stopLoopReq);
            if (s != null) {
                s.stop();
            }
        }
    }

}
