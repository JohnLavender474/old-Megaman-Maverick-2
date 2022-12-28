package com.megaman.game.events;

public enum EventType {
    GAME_PAUSE,
    GAME_RESUME,

    PLAYER_SPAWN,
    PLAYER_DEAD,

    BEGIN_GAME_ROOM_TRANS,
    CONTINUE_GAME_ROOM_TRANS,
    END_GAME_ROOM_TRANS,
    NEXT_GAME_ROOM_REQ,

    GATE_INIT_OPENING,
    GATE_FINISH_OPENING,
    GATE_INIT_CLOSING,
    GATE_FINISH_CLOSING,

    ENTER_BOSS_ROOM,
    BOSS_DROP_DOWN
}
