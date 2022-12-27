package com.megaman.game.screens.menus;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.controllers.ControllerBtn;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.utils.ConstFuncs;
import com.megaman.game.utils.enums.Direction;
import lombok.Getter;

public abstract class MenuScreen extends ScreenAdapter {

    protected final ControllerManager ctrlMan;
    protected final OrthographicCamera uiCam;
    protected final AudioManager audioMan;
    protected final AssetsManager assMan;
    protected final SpriteBatch batch;
    protected final MegamanGame game;

    private final String firstBtnKey;
    private final ObjectMap<String, MenuButton> menuButtons;

    @Getter
    private String currBtnKey;
    @Getter
    private boolean selectionMade;

    public MenuScreen(MegamanGame game, String firstBtnKey) {
        this.game = game;
        batch = game.getBatch();
        assMan = game.getAssMan();
        audioMan = game.getAudioMan();
        ctrlMan = game.getCtrlMan();
        uiCam = game.getUiCam();
        menuButtons = defineMenuButtons();
        currBtnKey = this.firstBtnKey = firstBtnKey;
    }

    protected abstract ObjectMap<String, MenuButton> defineMenuButtons();

    protected void onAnyMovement() {
    }

    protected void onAnySelection() {
    }

    public void setMenuButton(String menuButtonKey) {
        currBtnKey = menuButtonKey;
    }

    @Override
    public void show() {
        selectionMade = false;
        setMenuButton(firstBtnKey);
        uiCam.position.set(ConstFuncs.getCamInitPos());
    }

    @Override
    public void render(float delta) {
        if (selectionMade || game.isPaused()) {
            return;
        }
        MenuButton menuButton = menuButtons.get(currBtnKey);
        if (menuButton != null) {
            Direction dir = null;
            if (ctrlMan.isJustPressed(ControllerBtn.DPAD_UP)) {
                dir = Direction.UP;
            } else if (ctrlMan.isJustPressed(ControllerBtn.DPAD_DOWN)) {
                dir = Direction.DOWN;
            } else if (ctrlMan.isJustPressed(ControllerBtn.DPAD_LEFT)) {
                dir = Direction.LEFT;
            } else if (ctrlMan.isJustPressed(ControllerBtn.DPAD_RIGHT)) {
                dir = Direction.RIGHT;
            }
            if (dir != null) {
                onAnyMovement();
                menuButton.onNavigate(dir, delta);
            }
            if (ctrlMan.isJustPressed(ControllerBtn.START) || ctrlMan.isJustPressed(ControllerBtn.X)) {
                onAnySelection();
                selectionMade = menuButton.onSelect(delta);
            }
        }
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
