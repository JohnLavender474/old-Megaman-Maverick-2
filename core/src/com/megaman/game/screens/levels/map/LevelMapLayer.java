package com.megaman.game.screens.levels.map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum LevelMapLayer {

    TEST("Test"),
    BLOCKS("Blocks"),
    SPECIAL("Special"),
    HAZARDS("Hazards"),
    GAME_ROOMS("GameRooms"),
    BACKGROUNDS("Backgrounds"),
    ENEMY_SPAWNS("EnemySpawns"),
    PLAYER_SPAWNS("PlayerSpawns"),
    DEATH_SENSORS("DeathSensors");

    public final String name;

}
