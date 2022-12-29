package com.megaman.game.screens.levels.handlers.state;

import com.badlogic.gdx.utils.OrderedMap;
import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.System;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.sprites.SpriteSystem;

public class LevelStateHandler {

    private final GameEngine engine;
    private final AudioManager audioMan;

    private OrderedMap<Class<? extends System>, Boolean> sysStatesOnPause;

    public LevelStateHandler(MegamanGame game) {
        engine = game.getGameEngine();
        audioMan = game.getAudioMan();
    }

    public void pause() {
        sysStatesOnPause = engine.getStates();
        engine.setAll(false);
        engine.set(true, SpriteSystem.class);
        audioMan.pauseSound();
        audioMan.pauseMusic();
        audioMan.playSound(SoundAsset.PAUSE_SOUND);
    }

    public void resume() {
        engine.set(sysStatesOnPause);
        audioMan.resumeSound();
        audioMan.resumeMusic();
        audioMan.playSound(SoundAsset.PAUSE_SOUND);
    }

}
