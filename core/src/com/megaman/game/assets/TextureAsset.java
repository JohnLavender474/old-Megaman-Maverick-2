package com.megaman.game.assets;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum TextureAsset implements Asset {

    // Colors
    COLORS("Colors.txt"),

    // Enemies
    MET("Met.txt"),
    SPRING_HEAD("SpringHead.txt"),
    ENEMIES_1("Enemies1.txt"),

    // Objects
    OBJECTS("Objects.txt"),

    // Hazards
    SAW("SawBeam.txt"),
    SPIKES("Spikes.txt"),
    HAZARDS_1("Hazards1.txt"),

    // Weapons
    FIRE("Fire.txt"),
    ELECTRIC("Electric.txt"),
    MEGAMAN_CHARGED_SHOT("MegamanChargedShot.txt"),
    MEGAMAN_HALF_CHARGED_SHOT("MegamanHalfChargedShot.txt"),

    // Items
    ITEMS("Items.txt"),

    // Environment
    SNOW("Snow.txt"),
    DOORS("Door.txt"),
    WATER("Water.txt"),
    DECORATIONS("Decorations.txt"),

    // Backgrounds
    BACKGROUNDS_1("Backgrounds1.txt"),
    BACKGROUNDS_2("Backgrounds2.txt"),

    // UI
    BITS("HealthAndWeaponBits.txt"),
    STAGE_SELECT("StageSelect.txt"),
    BOSS_FACES("BossFaces.txt"),
    MEGAMAN_FACES("MegamanFaces.txt"),
    PAUSE_MENU("PauseMenu.txt"),
    MEGAMAN_MAIN_MENU("MegamanMainMenu.txt"),

    // Megaman
    MEGAMAN("Megaman.txt"),
    MEGAMAN_FIRE("MegamanFire.txt"),
    CHARGE_ORBS("ChargeOrbs.txt"),

    // Tiles
    CUSTOM_TILES_1("CustomTiles1.txt"),
    CONVEYOR_BELT("ConveyorBelt.txt"),

    // Bosses
    TIMBER_WOMAN("TimberWoman.txt"),
    DISTRIBUTOR_MAN("DistributorMan.txt"),
    ROASTER_MAN("RoasterMan.txt"),
    MISTER_MAN("MisterMan.txt"),
    BLUNT_MAN("BluntMan.txt"),
    NUKE_MAN("NukeMan.txt"),
    FRIDGE_MAN("FridgeMan.txt"),
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
