package com.megaman.game.assets;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum TextureAsset implements Asset {

    // Colors
    COLORS("Colors.txt"),

    // Enemies
    ENEMIES_1("Enemies1.txt"),

    // Specials
    SPECIALS_1("Specials1.txt"),

    // Hazards
    HAZARDS_1("Hazards1.txt"),

    // Weapons
    PROJECTILES_1("Projectiles1.txt"),
    MEGAMAN_CHARGED_SHOT("MegamanChargedShot.txt"),

    // Items
    ITEMS_1("Items1.txt"),

    // Environs
    ENVIRONS_1("Environs1.txt"),

    // Explosions
    EXPLOSIONS_1("Explosions1.txt"),

    // Backgrounds
    BACKGROUNDS_1("Backgrounds1.txt"),
    BACKGROUNDS_2("Backgrounds2.txt"),

    // UI
    UI_1("Ui1.txt"),
    FACES_1("Faces1.txt"),

    // Megaman
    MEGAMAN("Megaman.txt"),
    MEGAMAN_MAVERICK("MegamanMaverick.txt"),
    MEGAMAN_FIRE("MegamanFire.txt"),

    // Platforms
    GATES("Gates.txt"),
    PLATFORMS_1("Platforms1.txt"),

    // Main bosses
    TIMBER_WOMAN("TimberWoman.txt"),
    WINTRY_MAN("WintryMan.txt"),
    DISTRIBUTOR_MAN("DistributorMan.txt"),
    ROASTER_MAN("RoasterMan.txt"),
    MISTER_MAN("MisterMan.txt"),
    BLUNT_MAN("BluntMan.txt"),
    NUKE_MAN("NukeMan.txt"),
    FREEZER_MAN("FreezerMan.txt"),
    RODENT_MAN("RodentMan.txt"),
    PRECIOUS_MAN("PreciousMan.txt"),
    MICROWAVE_MAN("MicrowaveMan.txt"),

    // Other bosses
    GUTS_TANK("GutsTank.txt");

    public static final String PREFIX = "sprites/SpriteSheets/";

    private final String src;

    @Override
    public String getSrc() {
        return PREFIX + src;
    }

    @Override
    public Class<?> getAssClass() {
        return TextureAtlas.class;
    }

}
