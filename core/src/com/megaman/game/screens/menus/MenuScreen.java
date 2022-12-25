package com.megaman.game.screens.menus;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.controllers.ControllerBtn;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.ViewVals;
import com.megaman.game.world.WorldVals;
import lombok.Getter;
import lombok.Setter;

public abstract class MenuScreen extends ScreenAdapter {

    protected final ControllerManager ctrlMan;
    protected final OrthographicCamera uiCam;
    protected final AudioManager audioMan;
    protected final AssetsManager assMan;
    protected final Viewport uiViewport;
    protected final SpriteBatch batch;
    protected final MegamanGame game;

    private final String firstBtnKey;
    private final ObjectMap<String, MenuButton> menuButtons;

    @Getter
    @Setter
    public Music music;
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
        uiCam = new OrthographicCamera();
        menuButtons = defineMenuButtons();
        currBtnKey = this.firstBtnKey = firstBtnKey;
        uiViewport = new FitViewport(ViewVals.VIEW_WIDTH, ViewVals.VIEW_HEIGHT, uiCam);
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
        setMenuButton(firstBtnKey);
        selectionMade = false;
        music.play();
        Vector3 camPos = uiCam.position;
        camPos.x = (ViewVals.VIEW_WIDTH * WorldVals.PPM) / 2f;
        camPos.y = (ViewVals.VIEW_HEIGHT * WorldVals.PPM) / 2f;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (isSelectionMade()) {
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
        uiViewport.apply();
    }

    @Override
    public void dispose() {
        if (music != null) {
            music.stop();
        }
    }

    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height);
    }

}
