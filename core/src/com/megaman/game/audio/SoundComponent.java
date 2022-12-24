package com.megaman.game.audio;

import com.megaman.game.Component;
import com.megaman.game.assets.SoundAsset;

import java.util.LinkedList;
import java.util.Queue;

public class SoundComponent implements Component {

    public Queue<SoundAsset> playReqs = new LinkedList<>();
    public Queue<SoundAsset> stopReqs = new LinkedList<>();

    public void requestToPlay(SoundAsset ass) {
        playReqs.add(ass);
    }

    public void requestToStop(SoundAsset ass) {
        stopReqs.add(ass);
    }

}
