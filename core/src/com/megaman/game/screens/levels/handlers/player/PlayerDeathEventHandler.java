package com.megaman.game.screens.levels.handlers.player;

import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.controllers.ControllerSystem;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventManager;
import com.megaman.game.events.EventType;
import com.megaman.game.utils.interfaces.Initializable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Timer;

public class PlayerDeathEventHandler implements Initializable, Updatable {

    private static final float ON_DEATH_DELAY = 4f;

    private final Megaman megaman;
    private final Timer deathTimer;
    private final GameEngine engine;
    private final EventManager eventMan;
    private final AudioManager audioMan;

    public PlayerDeathEventHandler(MegamanGame game) {
        audioMan = game.getAudioMan();
        eventMan = game.getEventMan();
        engine = game.getGameEngine();
        megaman = game.getMegaman();
        deathTimer = new Timer(ON_DEATH_DELAY, true);
    }

    public boolean isFinished() {
        return deathTimer.isFinished();
    }

    @Override
    public void init() {
        deathTimer.reset();
        engine.set(false, ControllerSystem.class);
        megaman.body.gravityOn = false;
        megaman.setReady(false);
        audioMan.playSound(SoundAsset.MEGAMAN_DEFEAT_SOUND);
    }

    @Override
    public void update(float delta) {
        deathTimer.update(delta);
        if (deathTimer.isJustFinished()) {
            eventMan.submit(new Event(EventType.PLAYER_DONE_DYIN));
        }
    }

}
