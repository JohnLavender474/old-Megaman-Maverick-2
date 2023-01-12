package com.megaman.game.screens.levels.handlers.player;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.ConstKeys;
import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.ViewVals;
import com.megaman.game.animations.Animation;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.controllers.ControllerSystem;
import com.megaman.game.entities.impl.megaman.Megaman;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventManager;
import com.megaman.game.events.EventType;
import com.megaman.game.screens.utils.TextHandle;
import com.megaman.game.sprites.SpriteDrawer;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.interfaces.Initializable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldVals;

public class PlayerSpawnEventHandler implements Initializable, Updatable, Drawable {

    private static final float PRE_BEAM_DUR = 1f;
    private static final float BEAM_DOWN_DUR = .5f;
    private static final float BEAM_TRANS_DUR = .2f;
    private static final float BLINK_READY_DUR = .125f;

    private final Megaman megaman;
    private final GameEngine engine;
    private final AudioManager audioMan;
    private final EventManager eventMan;
    private final OrthographicCamera uiCam;
    private final OrthographicCamera gameCam;

    private final Sprite beamSprite;
    private final TextureRegion beamReg;
    private final Animation beamLandAnim;

    private final TextHandle ready;

    private final Timer blinkTimer;
    private final Timer preBeamTimer;
    private final Timer beamDownTimer;
    private final Timer beamTransTimer;

    private boolean blinkReady;

    public PlayerSpawnEventHandler(MegamanGame game) {
        megaman = game.getMegaman();
        engine = game.getGameEngine();
        audioMan = game.getAudioMan();
        eventMan = game.getEventMan();
        uiCam = game.getUiCam();
        gameCam = game.getGameCam();
        beamSprite = new Sprite();
        beamSprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.MEGAMAN);
        beamReg = atlas.findRegion("Beam");
        beamLandAnim = new Animation(atlas.findRegion("BeamLand"), 2, .1f, false);
        blinkTimer = new Timer(BLINK_READY_DUR, true);
        preBeamTimer = new Timer(PRE_BEAM_DUR, true);
        beamDownTimer = new Timer(BEAM_DOWN_DUR, true);
        beamTransTimer = new Timer(BEAM_TRANS_DUR, true);
        ready = new TextHandle(new Vector2(
                ViewVals.VIEW_WIDTH * WorldVals.PPM / 2f,
                ViewVals.VIEW_HEIGHT * WorldVals.PPM / 2f),
                ConstKeys.READY.toUpperCase(), true, true);
    }

    public boolean isFinished() {
        return preBeamTimer.isFinished() && beamDownTimer.isFinished() && beamTransTimer.isFinished();
    }

    @Override
    public void init() {
        blinkReady = false;
        blinkTimer.reset();
        preBeamTimer.reset();
        beamDownTimer.reset();
        beamTransTimer.reset();
        beamLandAnim.reset();
        beamSprite.setPosition(-WorldVals.PPM, -WorldVals.PPM);
        audioMan.play();
        megaman.body.gravityOn = false;
        engine.set(false, ControllerSystem.class);
        eventMan.submit(new Event(EventType.PLAYER_SPAWN));
    }

    @Override
    public void update(float delta) {
        if (!preBeamTimer.isFinished()) {
            preBeam(delta);
        } else if (!beamDownTimer.isFinished()) {
            beamDown(delta);
        } else if (!beamTransTimer.isFinished()) {
            beamTrans(delta);
        }
        blinkTimer.update(delta);
        if (blinkTimer.isFinished()) {
            blinkReady = !blinkReady;
            blinkTimer.reset();
        }
    }

    private void preBeam(float delta) {
        preBeamTimer.update(delta);
        if (preBeamTimer.isJustFinished()) {
            beamSprite.setRegion(beamReg);
        }
    }

    private void beamDown(float delta) {
        beamDownTimer.update(delta);
        float startY = megaman.body.getY() + (ViewVals.VIEW_HEIGHT * WorldVals.PPM);
        float offsetY = (ViewVals.VIEW_HEIGHT * WorldVals.PPM) * beamDownTimer.getRatio();
        beamSprite.setCenterX(megaman.body.getCenter().x);
        beamSprite.setY(startY - offsetY);
    }

    private void beamTrans(float delta) {
        beamTransTimer.update(delta);
        beamLandAnim.update(delta);
        beamSprite.setRegion(beamLandAnim.getCurrRegion());
        if (beamTransTimer.isJustFinished()) {
            engine.set(true, ControllerSystem.class);
            megaman.body.gravityOn = true;
            megaman.setReady(true);
            eventMan.submit(new Event(EventType.PLAYER_READY));
            audioMan.play(SoundAsset.BEAM_IN_SOUND);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (blinkReady) {
            batch.setProjectionMatrix(uiCam.combined);
            ready.draw(batch);
        }
        if (preBeamTimer.isFinished() && (!beamDownTimer.isFinished() || !beamTransTimer.isFinished())) {
            batch.setProjectionMatrix(gameCam.combined);
            SpriteDrawer.draw(beamSprite, batch);
        }
    }

}
