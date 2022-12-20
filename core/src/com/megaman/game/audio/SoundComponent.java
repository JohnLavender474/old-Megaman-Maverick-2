package com.megaman.game.audio;

import com.megaman.game.Component;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.utils.interfaces.Updatable;

import java.util.LinkedList;
import java.util.Queue;

public class SoundComponent implements Component {

    public final Queue<SoundRequest> sReqs = new LinkedList<>();
    public final Queue<SoundAsset> loopsToStop = new LinkedList<>();

    public Updatable updatable;

    public void request(SoundAsset s) {
        request(s, false);
    }

    public void request(SoundAsset s, boolean loop) {
        sReqs.add(new SoundRequest(s, loop));
    }

    public void stopLoop(SoundAsset s) {
        loopsToStop.add(s);
    }

}
