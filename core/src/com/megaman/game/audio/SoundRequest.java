package com.megaman.game.audio;

import com.megaman.game.assets.SoundAsset;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SoundRequest {

    public SoundAsset sAss;
    public boolean loop;

    public SoundRequest(SoundAsset sAss) {
        this(sAss, false);
    }

}
