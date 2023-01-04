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
    FACES("Faces.txt"),

    // Megaman
    MEGAMAN("Megaman.txt"),
    MEGAMAN_FIRE("MegamanFire.txt"),

    // Platforms
    GATES("Gates.txt"),
    PLATFORMS_1("Platforms1.txt"),

    // Bosses
    TIMBER_WOMAN("TimberWoman.txt"),
    DISTRIBUTOR_MAN("DistributorMan.txt"),
    ROASTER_MAN("RoasterMan.txt"),
    MISTER_MAN("MisterMan.txt"),
    BLUNT_MAN("BluntMan.txt"),
    NUKE_MAN("NukeMan.txt"),
    FREEZER_MAN("FreezerMan.txt"),
    MICROWAVE_MAN("MicrowaveMan.txt");

    private static final String prefix = "sprites/SpriteSheets/";

    private final String src;

    @Override
    public String getSrc() {
        return prefix + src;
    }

    @Override
    public Class<?> getAssClass() {
        return TextureAtlas.class;
    }

}
