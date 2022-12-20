package com.megaman.game.screens.levels;

import com.megaman.game.assets.MusicAsset;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Level {

    TIMBER_WOMAN_LEVEL("", MusicAsset.MMZ_NEO_ARCADIA_MUSIC),
    TEST("test1.tmx", MusicAsset.MMZ_NEO_ARCADIA_MUSIC);

    public final String tmxFile;
    public final MusicAsset musicAsset;

}
