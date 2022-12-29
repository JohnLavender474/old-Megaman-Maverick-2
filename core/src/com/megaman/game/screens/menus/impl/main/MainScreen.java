package com.megaman.game.screens.menus.impl.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.ViewVals;
import com.megaman.game.assets.MusicAsset;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.screens.ScreenEnum;
import com.megaman.game.screens.menus.MenuButton;
import com.megaman.game.screens.menus.MenuScreen;
import com.megaman.game.screens.menus.utils.BlinkingArrow;
import com.megaman.game.screens.menus.utils.ScreenSlide;
import com.megaman.game.screens.utils.TextHandle;
import com.megaman.game.sprites.SpriteDrawer;
import com.megaman.game.utils.ConstFuncs;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldVals;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

public class MainScreen extends MenuScreen {

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum MainBtn {

        GAME_START("GAME START"),
        PASS_WORD("PASS WORD"),
        SETTINGS("SETTINGS"),
        CREDITS("CREDITS"),
        EXTRAS("EXTRAS"),
        EXIT("EXIT");

        public final String prompt;

    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum SettingsBtn {

        BACK("BACK"),
        MUSIC_VOLUME("MUSIC: "),
        SOUND_EFFECTS_VOLUME("SOUND: ");

        public final String prompt;

    }

    private static final float SETTINGS_ARROW_BLINK_DUR = .3f;
    private static final float SETTINGS_TRANS_DUR = .5f;
    private static final Vector3 SETTINGS_TRAJ = new Vector3(15f * WorldVals.PPM, 0f, 0f);

    private final Sprite pose;
    private final Sprite title;
    private final Sprite subtitle;
    private final ScreenSlide screenSlide;
    private final Array<TextHandle> fonts;
    private final Array<Sprite> settingsArrs;
    private final Timer settingsArrBlinkTimer;
    private final ObjectMap<String, BlinkingArrow> blinkArrs;

    private boolean settingsArrowBlink;

    public MainScreen(MegamanGame game) {
        super(game, MainBtn.GAME_START.name());
        pose = new Sprite();
        title = new Sprite();
        subtitle = new Sprite();
        fonts = new Array<>();
        settingsArrs = new Array<>();
        blinkArrs = new ObjectMap<>();
        screenSlide = new ScreenSlide(uiCam, SETTINGS_TRAJ, ConstFuncs.getCamInitPos(),
                ConstFuncs.getCamInitPos().add(SETTINGS_TRAJ), SETTINGS_TRANS_DUR, true);
        settingsArrBlinkTimer = new Timer(SETTINGS_ARROW_BLINK_DUR);
        float row = .175f * WorldVals.PPM;
        for (MainBtn mainBtn : MainBtn.values()) {
            fonts.add(new TextHandle(new Vector2(2f * WorldVals.PPM, row * WorldVals.PPM), mainBtn.prompt));
            Vector2 arrowCenter = new Vector2(1.5f * WorldVals.PPM, (row - (.0075f * WorldVals.PPM)) * WorldVals.PPM);
            blinkArrs.put(mainBtn.name(), new BlinkingArrow(assMan, arrowCenter));
            row -= WorldVals.PPM * .025f;
        }
        row = .4f * WorldVals.PPM;
        for (SettingsBtn settingsBtn : SettingsBtn.values()) {
            fonts.add(new TextHandle(new Vector2(17f * WorldVals.PPM, row * WorldVals.PPM), settingsBtn.prompt));
            Vector2 arrowCenter = new Vector2(16.5f * WorldVals.PPM, (row - (.0075f * WorldVals.PPM)) * WorldVals.PPM);
            blinkArrs.put(settingsBtn.name(), new BlinkingArrow(assMan, arrowCenter));
            row -= WorldVals.PPM * .025f;
        }
        fonts.add(new TextHandle(new Vector2(3f * WorldVals.PPM, .5f * WorldVals.PPM), "© OLD LAVY GENES, 20XX"));
        fonts.add(new TextHandle(new Vector2(21f * WorldVals.PPM, 12f * WorldVals.PPM),
                () -> "" + game.getAudioMan().getMusicVolume()));
        fonts.add(new TextHandle(new Vector2(21f * WorldVals.PPM, 11.2f * WorldVals.PPM),
                () -> "" + game.getAudioMan().getSoundVolume()));
        TextureRegion arrowRegion = game.getAssMan().getTextureRegion(TextureAsset.DECORATIONS, "Arrow");
        float y = 11.55f;
        for (int i = 0; i < 4; i++) {
            if (i != 0 && i % 2 == 0) {
                y -= .85f;
            }
            Sprite blinkingArrow = new Sprite(arrowRegion);
            blinkingArrow.setBounds(
                    (i % 2 == 0 ? 20.25f : 22.5f) * WorldVals.PPM,
                    y * WorldVals.PPM,
                    WorldVals.PPM / 2f,
                    WorldVals.PPM / 2f);
            blinkingArrow.setFlip(i % 2 == 0, false);
            settingsArrs.add(blinkingArrow);
        }
        TextureAtlas decorations = game.getAssMan().getTextureAtlas(TextureAsset.DECORATIONS);
        title.setRegion(decorations.findRegion("MegamanTitle"));
        title.setBounds(
                WorldVals.PPM,
                6.5f * WorldVals.PPM,
                14f * WorldVals.PPM,
                8f * WorldVals.PPM);
        TextureAtlas mainMenu = game.getAssMan().getTextureAtlas(TextureAsset.MEGAMAN_MAIN_MENU);
        subtitle.setRegion(mainMenu.findRegion("Subtitle8bit"));
        subtitle.setSize(
                8f * WorldVals.PPM,
                8f * WorldVals.PPM);
        subtitle.setCenter(
                ViewVals.VIEW_WIDTH * WorldVals.PPM / 2f,
                ViewVals.VIEW_HEIGHT * WorldVals.PPM / 2f);
        pose.setRegion(mainMenu.findRegion("MegamanPose"));
        pose.setBounds(
                5.5f * WorldVals.PPM,
                0f,
                10f * WorldVals.PPM,
                10f * WorldVals.PPM);
    }

    @Override
    public void show() {
        super.show();
        game.getAudioMan().play(MusicAsset.MM11_WILY_STAGE_MUSIC, true);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (!game.isPaused()) {
            screenSlide.update(delta);
            if (screenSlide.isJustFinished()) {
                screenSlide.reverse();
            }
            blinkArrs.get(getCurrBtnKey()).update(delta);
            settingsArrBlinkTimer.update(delta);
            if (settingsArrBlinkTimer.isFinished()) {
                settingsArrowBlink = !settingsArrowBlink;
                settingsArrBlinkTimer.reset();
            }
        }
        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        blinkArrs.get(getCurrBtnKey()).draw(batch);
        SpriteDrawer.draw(title, batch);
        SpriteDrawer.draw(subtitle, batch);
        SpriteDrawer.draw(pose, batch);
        for (TextHandle font : fonts) {
            font.draw(batch);
        }
        if (settingsArrowBlink) {
            for (Sprite a : settingsArrs) {
                SpriteDrawer.draw(a, batch);
            }
        }
        batch.end();
    }

    @Override
    protected void onAnyMovement() {
        audioMan.play(SoundAsset.CURSOR_MOVE_BLOOP_SOUND);
    }

    @Override
    protected void onAnySelection() {
        audioMan.play(SoundAsset.SELECT_PING_SOUND);
    }

    @Override
    protected ObjectMap<String, MenuButton> defineMenuButtons() {
        return new ObjectMap<>() {{
            put(MainBtn.GAME_START.name(), new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    game.setScreen(game.getScreen(ScreenEnum.BOSS_SELECT));
                    return true;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case UP -> setMenuButton(MainBtn.EXIT.name());
                        case DOWN -> setMenuButton(MainBtn.PASS_WORD.name());
                    }
                }
            });
            put(MainBtn.PASS_WORD.name(), new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    // TODO: Set to password screen
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case UP -> setMenuButton(MainBtn.GAME_START.name());
                        case DOWN -> setMenuButton(MainBtn.SETTINGS.name());
                    }
                }
            });
            put(MainBtn.SETTINGS.name(), new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    // TODO: Set to settings screen
                    screenSlide.init();
                    setMenuButton(SettingsBtn.BACK.name());
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case UP -> setMenuButton(MainBtn.PASS_WORD.name());
                        case DOWN -> setMenuButton(MainBtn.CREDITS.name());
                    }
                }
            });
            put(MainBtn.CREDITS.name(), new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case UP -> setMenuButton(MainBtn.SETTINGS.name());
                        case DOWN -> setMenuButton(MainBtn.EXTRAS.name());
                    }
                }
            });
            put(MainBtn.EXTRAS.name(), new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    game.setScreen(game.getScreen(ScreenEnum.EXTRAS));
                    return true;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case UP -> setMenuButton(MainBtn.CREDITS.name());
                        case DOWN -> setMenuButton(MainBtn.EXIT.name());
                    }
                }
            });
            put(MainBtn.EXIT.name(), new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    // TODO: Pop up dialog asking to confirm exit game, press X to accept, any other to abort
                    Gdx.app.exit();
                    return true;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case UP -> setMenuButton(MainBtn.EXTRAS.name());
                        case DOWN -> setMenuButton(MainBtn.GAME_START.name());
                    }
                }
            });
            put(SettingsBtn.BACK.name(), new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    screenSlide.init();
                    setMenuButton(MainBtn.SETTINGS.name());
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case UP -> setMenuButton(SettingsBtn.SOUND_EFFECTS_VOLUME.name());
                        case DOWN -> setMenuButton(SettingsBtn.MUSIC_VOLUME.name());
                    }
                }
            });
            put(SettingsBtn.MUSIC_VOLUME.name(), new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case LEFT -> {
                            int volume = audioMan.getMusicVolume();
                            volume = volume == 0 ? 10 : volume - 1;
                            audioMan.setMusicVolume(volume);
                        }
                        case RIGHT -> {
                            int volume = audioMan.getMusicVolume();
                            volume = volume == 10 ? 0 : volume + 1;
                            audioMan.setMusicVolume(volume);
                        }
                        case UP -> setMenuButton(SettingsBtn.BACK.name());
                        case DOWN -> setMenuButton(SettingsBtn.SOUND_EFFECTS_VOLUME.name());
                    }
                }
            });
            put(SettingsBtn.SOUND_EFFECTS_VOLUME.name(), new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case LEFT -> {
                            int volume = audioMan.getSoundVolume();
                            volume = volume == 0 ? 10 : volume - 1;
                            audioMan.setSoundVolume(volume);
                        }
                        case RIGHT -> {
                            int volume = audioMan.getSoundVolume();
                            volume = volume == 10 ? 0 : volume + 1;
                            audioMan.setSoundVolume(volume);
                        }
                        case UP -> setMenuButton(SettingsBtn.MUSIC_VOLUME.name());
                        case DOWN -> setMenuButton(SettingsBtn.BACK.name());
                    }
                }
            });
        }};
    }

}

