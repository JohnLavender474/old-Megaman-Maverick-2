package com.megaman.game.screens.levels;

import com.megaman.game.assets.MusicAsset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Level {

    TIMBER_WOMAN_LEVEL("", MusicAsset.MMZ_NEO_ARCADIA_MUSIC),
    TEST("Test2.tmx", MusicAsset.MMZ_NEO_ARCADIA_MUSIC);

    private final String tmxFile;
    @Getter
    private final MusicAsset musicAsset;

    public String getTmxFile() {
        return "tiledmaps/tmx/" + tmxFile;
    }

}
