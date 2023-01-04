package com.megaman.game.entities.megaman.weapons;

import com.megaman.game.assets.TextureAsset;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public enum MegamanWeapon {

    MEGA_BUSTER(0, 0, 0, "Mega Buster", "Bit", "", TextureAsset.MEGAMAN),
    FLAME_TOSS(3, 5, 8, "Flame Toss", "RedBit", "", TextureAsset.MEGAMAN_FIRE);

    public final int cost;
    public final int halfChargedCost;
    public final int fullyChargedCost;

    public final String weaponText;
    public final String weaponBitSrc;
    public final String weaponSpriteSrc;

    public final TextureAsset megamanAss;

}
