package com.megaman.game.entities.megaman.weapons;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public enum MegamanWeapon {

    MEGA_BUSTER(0, 0, 0, "Mega Buster", ""),
    FLAME_TOSS(3, 5, 8, "Flame Toss", "");

    public final int cost;
    public final int halfChargedCost;
    public final int fullyChargedCost;

    public final String weaponText;
    public final String weaponSpriteSrc;

}
