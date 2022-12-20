package com.megaman.game.screens.menus;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.controllers.ControllerBtn;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.ViewVals;
import com.megaman.game.world.WorldConstVals;
import lombok.Getter;

public abstract class MenuScreen extends ScreenAdapter {

    protected final ControllerManager controllerManager;
    protected final AssetsManager assetsManager;
    protected final OrthographicCamera uiCam;
    protected final SpriteBatch batch;

    protected Music music;

    private final String firstBtnKey;
    private final ObjectMap<String, MenuButton> menuButtons;

    @Getter
    private String currBtnKey;
    @Getter
    private boolean selectionMade;

    public MenuScreen(MegamanGame game, String musicSrc, String firstBtnKey) {
        this.controllerManager = game.getCtrlMan();
        this.assetsManager = game.getAssMan();
        this.music = assetsManager.getAsset(musicSrc, Music.class);
        this.uiCam = game.getViewportMan().getCam(ViewportType.UI);
        this.currBtnKey = this.firstBtnKey = firstBtnKey;
        this.batch = game.getBatch();
        this.menuButtons = defineMenuButtons();
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
        // gameContext.setDoUpdateController(true);
        Vector3 camPos = uiCam.position;
        camPos.x = (ViewVals.VIEW_WIDTH * WorldConstVals.PPM) / 2f;
        camPos.y = (ViewVals.VIEW_HEIGHT * WorldConstVals.PPM) / 2f;
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
            if (controllerManager.isJustPressed(ControllerBtn.DPAD_UP)) {
                dir = Direction.DIR_UP;
            } else if (controllerManager.isJustPressed(ControllerBtn.DPAD_DOWN)) {
                dir = Direction.DIR_DOWN;
            } else if (controllerManager.isJustPressed(ControllerBtn.DPAD_LEFT)) {
                dir = Direction.DIR_LEFT;
            } else if (controllerManager.isJustPressed(ControllerBtn.DPAD_RIGHT)) {
                dir = Direction.DIR_RIGHT;
            }
            if (dir != null) {
                onAnyMovement();
                menuButton.onNavigate(dir, delta);
            }
            if (controllerManager.isJustPressed(ControllerBtn.START) || controllerManager.isJustPressed(ControllerBtn.X)) {
                onAnySelection();
                selectionMade = menuButton.onSelect(delta);
            }
        }
    }

    @Override
    public void dispose() {
        if (music != null) {
            music.stop();
        }
    }

}
