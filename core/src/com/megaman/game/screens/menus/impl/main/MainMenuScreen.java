package com.megaman.game.screens.menus.impl.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.MusicAsset;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.screens.menus.MenuButton;
import com.megaman.game.screens.menus.MenuScreen;
import com.megaman.game.screens.menus.utils.BlinkingArrow;
import com.megaman.game.screens.menus.utils.ScreenSlide;
import com.megaman.game.ui.TextHandle;
import com.megaman.game.utils.ConstFuncs;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.ViewVals;
import com.megaman.game.world.WorldConstVals;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.megaman.game.screens.menus.impl.main.MainMenuScreen.MainMenuButton.*;
import static com.megaman.game.screens.menus.impl.main.MainMenuScreen.SettingsButton.*;

public class MainMenuScreen extends MenuScreen {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum MainMenuButton {

        GAME_START("GAME START"),
        PASS_WORD("PASS WORD"),
        SETTINGS("SETTINGS"),
        CREDITS("CREDITS"),
        EXTRAS("EXTRAS"),
        EXIT("EXIT");

        private final String prompt;

    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum SettingsButton {

        BACK("BACK"),
        MUSIC_VOLUME("MUSIC: "),
        SOUND_EFFECTS_VOLUME("SOUND: ");

        private final String prompt;

    }

    private static final Vector3 SETTINGS_TRANS = new Vector3(15f, 0f, 0f).scl(WorldConstVals.PPM);

    private final ScreenSlide screenSlide;

    private final Sprite pose = new Sprite();
    private final Sprite title = new Sprite();
    private final Sprite subtitle = new Sprite();

    private final Timer settingsArrowTimer = new Timer(.3f);

    private final Array<TextHandle> fonts = new Array<>();
    private final Array<Sprite> settingsArrows = new Array<>();

    private final ObjectMap<String, BlinkingArrow> blinkingArrows = new ObjectMap<>();

    private boolean settingsArrowBlink;

    public MainMenuScreen(MegamanGame game) {
        super(game, MusicAsset.MM11_WILY_STAGE_MUSIC.getSrc(), GAME_START.getPrompt());
        // screen slide
        screenSlide = new ScreenSlide(uiCam, SETTINGS_TRANS, ConstFuncs.getCamInitPos(),
                ConstFuncs.getCamInitPos().add(SETTINGS_TRANS), .5f, true);
        // buttons and arrows
        float row = .175f * WorldConstVals.PPM;
        for (MainMenuButton mainMenuButton : MainMenuButton.values()) {
            fonts.add(new TextHandle(new Vector2(2f * WorldConstVals.PPM, row * WorldConstVals.PPM),
                    mainMenuButton::getPrompt));
            Vector2 arrowCenter = new Vector2(1.5f * WorldConstVals.PPM,
                    (row - (.0075f * WorldConstVals.PPM)) * WorldConstVals.PPM);
            blinkingArrows.put(mainMenuButton.name(), new BlinkingArrow(assetsManager, arrowCenter));
            row -= WorldConstVals.PPM * .025f;
        }
        // fonts
        row = .4f * WorldConstVals.PPM;
        for (SettingsButton settingsButton : SettingsButton.values()) {
            fonts.add(new TextHandle(new Vector2(17f * WorldConstVals.PPM, row * WorldConstVals.PPM),
                    settingsButton::getPrompt));
            Vector2 arrowCenter = new Vector2(16.5f * WorldConstVals.PPM,
                    (row - (.0075f * WorldConstVals.PPM)) * WorldConstVals.PPM);
            blinkingArrows.put(settingsButton.name(), new BlinkingArrow(assetsManager, arrowCenter));
            row -= WorldConstVals.PPM * .025f;
        }
        fonts.add(new TextHandle(new Vector2(3f * WorldConstVals.PPM, .5f * WorldConstVals.PPM),
                () -> "© OLD LAVY GENES, 20XX"));
        /*n
        fonts.add(new TextHandle(new Vector2(21f * WorldConstVals.PPM, 12f * WorldConstVals.PPM),
                () -> "" + assMan.getMusicVolume()));
        fonts.add(new TextHandle(new Vector2(21f * WorldConstVals.PPM, 11.2f * WorldConstVals.PPM),
                () -> "" + assMan.getSoundEffectsVolume()));
         */
        TextureRegion arrowRegion = assetsManager.getAsset(TextureAsset.DECORATIONS.getSrc(), TextureAtlas.class)
                .findRegion("Arrow");
        // settings blinking arrows
        float y = 11.55f;
        for (int i = 0; i < 4; i++) {
            if (i != 0 && i % 2 == 0) {
                y -= .85f;
            }
            Sprite blinkingArrow = new Sprite(arrowRegion);
            blinkingArrow.setBounds((i % 2 == 0 ? 20.25f : 22.5f) * WorldConstVals.PPM, y * WorldConstVals.PPM,
                    WorldConstVals.PPM / 2f, WorldConstVals.PPM / 2f);
            if (i % 2 == 0) {
                blinkingArrow.setFlip(true, false);
            }
            settingsArrows.add(blinkingArrow);
        }
        // decorations
        TextureAtlas decorations = assetsManager.getAsset(TextureAsset.DECORATIONS.getSrc(), TextureAtlas.class);
        title.setRegion(decorations.findRegion("MegamanTitle"));
        title.setBounds(WorldConstVals.PPM, 6.5f * WorldConstVals.PPM,
                14f * WorldConstVals.PPM, 8f * WorldConstVals.PPM);
        TextureAtlas mainMenu = assetsManager.getAsset(TextureAsset.MEGAMAN_MAIN_MENU.getSrc(), TextureAtlas.class);
        subtitle.setRegion(mainMenu.findRegion("Subtitle8bit"));
        subtitle.setSize(8f * WorldConstVals.PPM, 8f * WorldConstVals.PPM);
        subtitle.setCenter(ViewVals.VIEW_WIDTH * WorldConstVals.PPM / 2f,
                ViewVals.VIEW_HEIGHT * WorldConstVals.PPM / 2f);
        pose.setRegion(mainMenu.findRegion("MegamanPose"));
        pose.setBounds(5.5f * WorldConstVals.PPM, 0f, 10f * WorldConstVals.PPM, 10f * WorldConstVals.PPM);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        // arrows and sprites
        BlinkingArrow blinkingArrow = blinkingArrows.get(getCurrBtnKey());
        blinkingArrow.update(delta);
        blinkingArrow.draw(batch);
        title.draw(batch);
        subtitle.draw(batch);
        pose.draw(batch);
        // fonts
        fonts.forEach(fontHandle -> fontHandle.draw(batch));
        settingsArrowTimer.update(delta);
        if (settingsArrowTimer.isFinished()) {
            settingsArrowBlink = !settingsArrowBlink;
            settingsArrowTimer.reset();
        }
        if (settingsArrowBlink) {
            settingsArrows.forEach(s -> s.draw(batch));
        }
        batch.end();
        // screen slide
        screenSlide.update(delta);
        if (screenSlide.isJustFinished()) {
            screenSlide.reverse();
        }
    }

    @Override
    protected void onAnyMovement() {
        Sound sound = assetsManager.getAsset(SoundAsset.CURSOR_MOVE_BLOOP_SOUND.getSrc(), Sound.class);
        // TODO: Set volume
        sound.play();
    }

    @Override
    protected void onAnySelection() {
        Sound sound = assetsManager.getAsset(SoundAsset.SELECT_PING_SOUND.getSrc(), Sound.class);
        // TODO: Set volume
        sound.play();
    }

    @Override
    protected ObjectMap<String, MenuButton> defineMenuButtons() {
        return new ObjectMap<>() {{
            put(GAME_START.name(), new MenuButton() {

                @Override
                public boolean onSelect(float delta) {
                    // gameContext.setScreen(BOSS_SELECT);
                    return true;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case DIR_DOWN -> setMenuButton(PASS_WORD.name());
                        case DIR_UP -> setMenuButton(EXIT.name());
                    }
                }

            });
            put(PASS_WORD.name(), new MenuButton() {

                @Override
                public boolean onSelect(float delta) {
                    // TODO: Set to password screen
                    return true;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case DIR_UP -> setMenuButton(GAME_START.name());
                        case DIR_DOWN -> setMenuButton(SETTINGS.name());
                    }
                }

            });
            put(SETTINGS.name(), new MenuButton() {

                @Override
                public boolean onSelect(float delta) {
                    // TODO: Set to settings screen
                    screenSlide.init();
                    setMenuButton(BACK.name());
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case DIR_UP -> setMenuButton(PASS_WORD.name());
                        case DIR_DOWN -> setMenuButton(CREDITS.name());
                    }
                }

            });
            put(CREDITS.name(), new MenuButton() {

                @Override
                public boolean onSelect(float delta) {
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case DIR_UP -> setMenuButton(SETTINGS.name());
                        case DIR_DOWN -> setMenuButton(EXTRAS.name());
                    }
                }

            });
            put(EXTRAS.name(), new MenuButton() {

                @Override
                public boolean onSelect(float delta) {
                    // gameContext.setScreen(GameScreen.EXTRAS);
                    return true;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case DIR_UP -> setMenuButton(CREDITS.name());
                        case DIR_DOWN -> setMenuButton(EXIT.name());
                    }
                }

            });
            put(EXIT.name(), new MenuButton() {

                @Override
                public boolean onSelect(float delta) {
                    // TODO: Pop up dialog asking to confirm exit game, press X to isMasking, any other to abort
                    Gdx.app.exit();
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case DIR_UP -> setMenuButton(EXTRAS.name());
                        case DIR_DOWN -> setMenuButton(GAME_START.name());
                    }
                }

            });
            put(MUSIC_VOLUME.name(), new MenuButton() {

                @Override
                public boolean onSelect(float delta) {
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case DIR_LEFT -> {
                                /*
                                 int volume = gameContext.getMusicVolume();
                                 volume = volume == 0 ? 10 : volume - 1;
                                 gameContext.setMusicVolume(volume);
                                */
                        }
                        case DIR_RIGHT -> {
                                /*
                                int volume = gameContext.getMusicVolume();
                                volume = volume == 10 ? 0 : volume + 1;
                                gameContext.setMusicVolume(volume);
                                 */
                        }
                        case DIR_UP -> setMenuButton(BACK.name());
                        case DIR_DOWN -> setMenuButton(SOUND_EFFECTS_VOLUME.name());
                    }
                }

            });
            put(SOUND_EFFECTS_VOLUME.name(), new MenuButton() {

                @Override
                public boolean onSelect(float delta) {
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case DIR_LEFT -> {
                                /*
                                int volume = gameContext.getSoundEffectsVolume();
                                volume = volume == 0 ? 10 : volume - 1;
                                gameContext.setSoundEffectsVolume(volume);
                                 */
                        }
                        case DIR_RIGHT -> {
                                /*
                                int volume = gameContext.getSoundEffectsVolume();
                                volume = volume == 10 ? 0 : volume + 1;
                                gameContext.setSoundEffectsVolume(volume);
                                 */
                        }
                        case DIR_UP -> setMenuButton(MUSIC_VOLUME.name());
                        case DIR_DOWN -> setMenuButton(BACK.name());
                    }
                }

            });
            put(BACK.name(), new MenuButton() {

                @Override
                public boolean onSelect(float delta) {
                    screenSlide.init();
                    setMenuButton(SETTINGS.name());
                    return false;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    switch (direction) {
                        case DIR_UP -> setMenuButton(SOUND_EFFECTS_VOLUME.name());
                        case DIR_DOWN -> setMenuButton(MUSIC_VOLUME.name());
                    }
                }

            });
        }};
    }

}
