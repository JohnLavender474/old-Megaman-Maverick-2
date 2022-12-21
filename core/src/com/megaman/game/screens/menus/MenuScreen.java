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
import com.megaman.game.controllers.ControllerBtn;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.ViewVals;
import com.megaman.game.world.WorldVals;
import lombok.Getter;

public abstract class MenuScreen extends ScreenAdapter {

    protected final AssetsManager assMan;
    protected final ControllerManager ctrlMan;
    protected final OrthographicCamera uiCam;
    protected final Viewport uiViewport;
    protected final SpriteBatch batch;
    protected Music music;

    private final String firstBtnKey;
    private final ObjectMap<String, MenuButton> menuButtons;

    @Getter
    private String currBtnKey;
    @Getter
    private boolean selectionMade;

    public MenuScreen(MegamanGame game, String musicSrc, String firstBtnKey) {
        this.ctrlMan = game.getCtrlMan();
        this.assMan = game.getAssMan();
        this.music = assMan.getAsset(musicSrc, Music.class);
        this.uiCam = new OrthographicCamera();
        this.uiViewport = new FitViewport(ViewVals.VIEW_WIDTH, ViewVals.VIEW_HEIGHT, uiCam);
        this.currBtnKey = this.firstBtnKey = firstBtnKey;
        this.batch = game.getBatch();
        this.menuButtons = defineMenuButtons();
    }

    protected abstract ObjectMap<String, MenuButton> defineMenuButtons();

    protected abstract void renderMenu();

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
    public final void render(float delta) {
        super.render(delta);
        if (isSelectionMade()) {
            return;
        }
        MenuButton menuButton = menuButtons.get(currBtnKey);
        if (menuButton != null) {
            Direction dir = null;
            if (ctrlMan.isJustPressed(ControllerBtn.DPAD_UP)) {
                dir = Direction.DIR_UP;
            } else if (ctrlMan.isJustPressed(ControllerBtn.DPAD_DOWN)) {
                dir = Direction.DIR_DOWN;
            } else if (ctrlMan.isJustPressed(ControllerBtn.DPAD_LEFT)) {
                dir = Direction.DIR_LEFT;
            } else if (ctrlMan.isJustPressed(ControllerBtn.DPAD_RIGHT)) {
                dir = Direction.DIR_RIGHT;
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
        renderMenu();
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
