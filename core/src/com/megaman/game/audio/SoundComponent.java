package com.megaman.game.audio;

import com.megaman.game.Component;
import com.megaman.game.assets.SoundAsset;

import java.util.LinkedList;
import java.util.Queue;

public class SoundComponent implements Component {

    public Queue<SoundAsset> playReqs;
    public Queue<SoundAsset> stopReqs;

    public SoundComponent() {
        playReqs = new LinkedList<>();
        stopReqs = new LinkedList<>();
    }

    public void requestToPlay(SoundAsset ass) {
        playReqs.add(ass);
    }

    public void requestToStop(SoundAsset ass) {
        stopReqs.add(ass);
    }

    @Override
    public void reset() {
        playReqs.clear();
        stopReqs.clear();
    }

}
