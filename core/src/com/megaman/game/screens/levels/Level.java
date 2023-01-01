package com.megaman.game.screens.levels;

import com.megaman.game.assets.MusicAsset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Level {

    TEST1("Test1.tmx", MusicAsset.MMZ_NEO_ARCADIA_MUSIC),
    TEST5("Test5.tmx", MusicAsset.MMZ_NEO_ARCADIA_MUSIC),
    TIMBER_WOMAN("", MusicAsset.MMZ_NEO_ARCADIA_MUSIC),
    DISTRIBUTOR_MAN("", MusicAsset.MMZ_NEO_ARCADIA_MUSIC),
    ROASTER_MAN("", MusicAsset.MMZ_NEO_ARCADIA_MUSIC),
    MISTER_MAN("", MusicAsset.MMZ_NEO_ARCADIA_MUSIC),
    BLUNT_MAN("", MusicAsset.MMZ_NEO_ARCADIA_MUSIC),
    NUKE_MAN("", MusicAsset.MMZ_NEO_ARCADIA_MUSIC),
    FRIDGE_MAN("FridgeMan.tmx", MusicAsset.MM8_FROST_MAN_ALT_MUSIC),
    MICROWAVE_MAN("", MusicAsset.MMZ_NEO_ARCADIA_MUSIC);

    private final String tmxFile;
    @Getter
    private final MusicAsset musicAss;

    public String getTmxFile() {
        return "tiledmaps/tmx/" + tmxFile;
    }

}
