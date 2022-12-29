package com.megaman.game.screens.other;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.MegamanGame;
import com.megaman.game.ViewVals;
import com.megaman.game.animations.Animation;
import com.megaman.game.assets.MusicAsset;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.backgrounds.Stars;
import com.megaman.game.screens.ScreenEnum;
import com.megaman.game.screens.levels.LevelScreen;
import com.megaman.game.screens.menus.impl.bosses.BEnum;
import com.megaman.game.screens.utils.TextHandle;
import com.megaman.game.sprites.SpriteDrawer;
import com.megaman.game.utils.objs.KeyValuePair;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldVals;

import java.util.LinkedList;
import java.util.Queue;

public class BIntroScreen extends ScreenAdapter {

    private static final float DUR = 7f;
    private static final float B_DROP = .25f;
    private static final float B_LET_DELAY = 1f;
    private static final float B_LET_DUR = .2f;
    private static final int STARS_N_BARS = 4;

    private final MegamanGame game;
    private final Camera uiCam;
    private final Timer bLettersDelay;
    private final Timer bLettersTimer;
    private final Timer durTimer;
    private final Timer bDropTimer;
    private final Animation barAnim;
    private final Array<Sprite> bars;
    private final Array<Stars> stars;
    private final TextHandle bText;

    private BEnum b;
    private Queue<Runnable> bLettersAnimQ;
    private KeyValuePair<Sprite, Queue<KeyValuePair<Animation, Timer>>> currBAnim;

    public BIntroScreen(MegamanGame game) {
        this.game = game;
        this.uiCam = game.getUiCam();
        bLettersDelay = new Timer(B_LET_DELAY);
        bLettersTimer = new Timer(B_LET_DUR);
        TextureRegion barReg = game.getAssMan().getTextureRegion(TextureAsset.STAGE_SELECT, "Bar");
        barAnim = new Animation(barReg, new float[]{.3f, .15f, .15f, .15f});
        bars = new Array<>() {{
            for (int i = 0; i < STARS_N_BARS; i++) {
                Sprite bar = new Sprite();
                bar.setBounds(
                        (i * ViewVals.VIEW_WIDTH * WorldVals.PPM / 3f) - 5f,
                        ViewVals.VIEW_HEIGHT * WorldVals.PPM / 3f,
                        (ViewVals.VIEW_WIDTH * WorldVals.PPM / 3f) + 5f,
                        ViewVals.VIEW_HEIGHT * WorldVals.PPM / 3f);
                add(bar);
            }
        }};
        stars = new Array<>() {{
            for (int i = 0; i < STARS_N_BARS; i++) {
                add(new Stars(
                        game,
                        0f,
                        i * WorldVals.PPM * ViewVals.VIEW_HEIGHT / 4f,
                        i + 1));
            }
        }};
        durTimer = new Timer(DUR);
        bDropTimer = new Timer(B_DROP);
        bText = new TextHandle(new Vector2(
                (ViewVals.VIEW_WIDTH * WorldVals.PPM / 3f) - WorldVals.PPM,
                ViewVals.VIEW_HEIGHT * WorldVals.PPM / 3f));
    }

    public void set(BEnum b) {
        this.b = b;
        Sprite s = new Sprite();
        Vector2 size = b.getSpriteSize();
        s.setSize(size.x * WorldVals.PPM, size.y * WorldVals.PPM);
        currBAnim = new KeyValuePair<>(s, b.getIntroAnimsQ(game.getAssMan().getTextureAtlas(b.ass)));
        bLettersAnimQ = new LinkedList<>();
        for (int i = 0; i < b.name.length(); i++) {
            final int finalI = i;
            bLettersAnimQ.add(() -> {
                bText.setText(b.name.substring(0, finalI + 1));
                if (Character.isWhitespace(b.name.charAt(finalI))) {
                    return;
                }
                game.getAudioMan().playSound(SoundAsset.THUMP_SOUND);
            });
        }
    }

    @Override
    public void show() {
        bText.clear();
        durTimer.reset();
        bDropTimer.reset();
        bLettersTimer.reset();
        bLettersDelay.reset();
        for (Stars s : stars) {
            s.resetPositions();
        }
        currBAnim.key().setPosition(
                ((ViewVals.VIEW_WIDTH / 2f) - 1.5f) * WorldVals.PPM,
                ViewVals.VIEW_HEIGHT * WorldVals.PPM);
        for (KeyValuePair<Animation, Timer> e : currBAnim.value()) {
            e.key().reset();
            e.value().reset();
        }
        game.getAudioMan().playMusic(MusicAsset.MM2_BOSS_INTRO_MUSIC, false);
    }

    @Override
    public void render(float delta) {
        if (durTimer.isFinished()) {
            LevelScreen l = game.getScreen(ScreenEnum.LEVEL, LevelScreen.class);
            l.set(b.level);
            game.setScreen(l);
            return;
        }
        Sprite bSprite = currBAnim.key();
        if (!game.isPaused()) {
            durTimer.update(delta);
            for (Stars s : stars) {
                s.update(delta);
            }
            barAnim.update(delta);
            for (Sprite b : bars) {
                b.setRegion(barAnim.getCurrRegion());
            }
            bDropTimer.update(delta);
            if (!bDropTimer.isFinished()) {
                bSprite.setY((ViewVals.VIEW_HEIGHT * WorldVals.PPM) -
                        (((ViewVals.VIEW_HEIGHT * WorldVals.PPM / 2f) + .85f * WorldVals.PPM) * bDropTimer.getRatio()));
            }
            if (bDropTimer.isJustFinished()) {
                bSprite.setY((ViewVals.VIEW_HEIGHT * WorldVals.PPM / 2f) - .85f * WorldVals.PPM);
            }
            bLettersDelay.update(delta);
            if (bLettersDelay.isFinished() && bDropTimer.isFinished() && !bLettersAnimQ.isEmpty()) {
                bLettersTimer.update(delta);
                if (bLettersTimer.isFinished()) {
                    bLettersAnimQ.poll().run();
                    bLettersTimer.reset();
                }
            }
            Queue<KeyValuePair<Animation, Timer>> bAnimQ = currBAnim.value();
            Timer t = bAnimQ.peek().value();
            if (bAnimQ.size() > 1 && t.isFinished()) {
                bAnimQ.poll();
            }
            t.update(delta);
            Animation bAnim = bAnimQ.peek().key();
            bAnim.update(delta);
            bSprite.setRegion(bAnim.getCurrRegion());
        }
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        for (Stars s : stars) {
            s.draw(batch);
        }
        for (Sprite b : bars) {
            SpriteDrawer.draw(b, batch);
        }
        SpriteDrawer.draw(bSprite, batch);
        bText.draw(batch);
        batch.end();
    }


    @Override
    public void pause() {
        game.getAudioMan().pauseSound();
        game.getAudioMan().pauseMusic();
    }

    @Override
    public void resume() {
        game.getAudioMan().resumeSound();
        game.getAudioMan().resumeMusic();
    }

    @Override
    public void dispose() {
        game.getAudioMan().stopMusic();
    }

}
