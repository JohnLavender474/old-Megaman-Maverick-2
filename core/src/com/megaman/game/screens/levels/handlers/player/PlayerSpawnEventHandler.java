package com.megaman.game.screens.levels.handlers.player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.ConstKeys;
import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.ViewVals;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.controllers.ControllerSystem;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventManager;
import com.megaman.game.events.EventType;
import com.megaman.game.screens.utils.TextHandle;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.interfaces.Initializable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldVals;

public class PlayerSpawnEventHandler implements Initializable, Updatable, Drawable {

    private static final float READY_DUR = 1.5f;
    private static final float BLINK_READY_DUR = .125f;

    private final Megaman megaman;
    private final GameEngine engine;
    private final AudioManager audioMan;
    private final EventManager eventMan;

    private final TextHandle ready;
    private final Timer readyTimer;
    private final Timer blinkTimer;

    private boolean blinkReady;

    public PlayerSpawnEventHandler(MegamanGame game) {
        megaman = game.getMegaman();
        engine = game.getGameEngine();
        audioMan = game.getAudioMan();
        eventMan = game.getEventMan();
        readyTimer = new Timer(READY_DUR);
        blinkTimer = new Timer(BLINK_READY_DUR);
        ready = new TextHandle(new Vector2(
                ViewVals.VIEW_WIDTH * WorldVals.PPM / 2f,
                ViewVals.VIEW_HEIGHT * WorldVals.PPM / 2f),
                ConstKeys.READY.toUpperCase(), true, true);
    }

    public boolean isFinished() {
        return readyTimer.isFinished();
    }

    @Override
    public void init() {
        blinkReady = false;
        readyTimer.reset();
        blinkTimer.reset();
        audioMan.playMusic();
        megaman.body.gravityOn = false;
        engine.set(false, ControllerSystem.class);
        eventMan.submit(new Event(EventType.PLAYER_SPAWN));
    }

    @Override
    public void update(float delta) {
        readyTimer.update(delta);
        if (readyTimer.isJustFinished()) {
            engine.set(true, ControllerSystem.class);
            megaman.body.gravityOn = true;
            megaman.setReady(true);
            eventMan.submit(new Event(EventType.PLAYER_READY));
            return;
        }
        if (readyTimer.isFinished()) {
            return;
        }
        blinkTimer.update(delta);
        if (blinkTimer.isFinished()) {
            blinkReady = !blinkReady;
            blinkTimer.reset();
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (blinkReady) {
            ready.draw(batch);
        }
    }

}
