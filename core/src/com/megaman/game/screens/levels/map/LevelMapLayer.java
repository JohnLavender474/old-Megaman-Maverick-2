package com.megaman.game.screens.levels.map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum LevelMapLayer {

    GATES("Gates"),
    DEATH("Death"),
    BLOCKS("Blocks"),
    SPECIAL("Special"),
    HAZARDS("Hazards"),
    GAME_ROOMS("GameRooms"),
    BACKGROUNDS("Backgrounds"),
    ENEMY_SPAWNS("EnemySpawns"),
    PLAYER_SPAWNS("PlayerSpawns");

    public final String name;

}
