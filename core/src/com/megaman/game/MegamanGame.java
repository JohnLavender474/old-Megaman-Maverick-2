package com.megaman.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.megaman.game.animations.AnimationSystem;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.audio.SoundSystem;
import com.megaman.game.behaviors.BehaviorSystem;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.controllers.ControllerSystem;
import com.megaman.game.controllers.CtrlBtn;
import com.megaman.game.cull.CullOnEventSystem;
import com.megaman.game.cull.CullOnOutOfBoundsSystem;
import com.megaman.game.entities.factories.EntityFactories;
import com.megaman.game.entities.impl.megaman.Megaman;
import com.megaman.game.events.EventManager;
import com.megaman.game.health.HealthSystem;
import com.megaman.game.movement.pendulum.PendulumSystem;
import com.megaman.game.movement.rotatingline.RotatingLineSystem;
import com.megaman.game.movement.trajectory.TrajectorySystem;
import com.megaman.game.pathfinding.PathfindingSystem;
import com.megaman.game.screens.ScreenEnum;
import com.megaman.game.screens.levels.Level;
import com.megaman.game.screens.levels.LevelScreen;
import com.megaman.game.screens.menus.impl.bosses.BSelectScreen;
import com.megaman.game.screens.menus.impl.main.MainScreen;
import com.megaman.game.screens.other.BIntroScreen;
import com.megaman.game.screens.utils.TextHandle;
import com.megaman.game.shapes.LineSystem;
import com.megaman.game.shapes.ShapeSystem;
import com.megaman.game.sprites.SpriteSystem;
import com.megaman.game.updatables.UpdatableSystem;
import com.megaman.game.utils.Logger;
import com.megaman.game.world.SpecialCollisionHandlerImpl;
import com.megaman.game.world.WorldContactListenerImpl;
import com.megaman.game.world.WorldSystem;
import com.megaman.game.world.WorldVals;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class MegamanGame implements ApplicationListener {

    public static final boolean DEBUG = true;

    private static final Logger logger = new Logger(MegamanGame.class, DEBUG);

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private Map<ScreenEnum, Screen> screens;

    private OrthographicCamera gameCam;
    private OrthographicCamera uiCam;
    private Viewport gameViewport;
    private Viewport uiViewport;

    private AssetsManager assMan;
    private AudioManager audioMan;
    private EventManager eventMan;
    private ControllerManager ctrlMan;

    private Megaman megaman;
    private GameEngine gameEngine;
    private EntityFactories entityFactories;

    private TextHandle fpsText;
    private boolean paused;

    private Screen screen;
    private Screen overlayScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        ctrlMan = new ControllerManager();
        assMan = new AssetsManager();
        assMan.loadAssets();
        audioMan = new AudioManager(assMan.getSound(), assMan.getMusic());
        eventMan = new EventManager();
        entityFactories = new EntityFactories(this);
        gameEngine = new GameEngine(
                new ControllerSystem(ctrlMan),
                new AnimationSystem(),
                new BehaviorSystem(),
                new WorldSystem(
                        new WorldContactListenerImpl(this),
                        new SpecialCollisionHandlerImpl(this)),
                new CullOnEventSystem(eventMan),
                new CullOnOutOfBoundsSystem(),
                new TrajectorySystem(),
                new PathfindingSystem(),
                new RotatingLineSystem(),
                new PendulumSystem(),
                new HealthSystem(),
                new UpdatableSystem(),
                new SpriteSystem(),
                new LineSystem(),
                new ShapeSystem(),
                new SoundSystem(audioMan));

        // TODO: set Megaman maverick only after intro stage
        megaman = new Megaman(this);
        setMegamanMaverick(true);

        float screenWidth = ViewVals.VIEW_WIDTH * WorldVals.PPM;
        float screenHeight = ViewVals.VIEW_HEIGHT * WorldVals.PPM;
        gameCam = new OrthographicCamera();
        uiCam = new OrthographicCamera();
        gameViewport = new FitViewport(screenWidth, screenHeight, gameCam);
        uiViewport = new FitViewport(screenWidth, screenHeight, uiCam);
        screens = new EnumMap<>(ScreenEnum.class);
        screens.put(ScreenEnum.LEVEL, new LevelScreen(this));
        screens.put(ScreenEnum.MAIN, new MainScreen(this));
        screens.put(ScreenEnum.BOSS_SELECT, new BSelectScreen(this));
        screens.put(ScreenEnum.BOSS_INTRO, new BIntroScreen(this));

        // setScreen(ScreenEnum.LEVEL, LevelScreen.class, s -> s.set(Level.RODENT_MAN));
        // setScreen(ScreenEnum.LEVEL, LevelScreen.class, s -> s.set(Level.CREW_MAN));
        // setScreen(ScreenEnum.LEVEL, LevelScreen.class, s -> s.set(Level.TEST5));
        setScreen(ScreenEnum.LEVEL, LevelScreen.class, s -> s.set(Level.TEST1));
        // setScreen(getScreen(ScreenEnum.MAIN));
        // setScreen(getScreen(ScreenEnum.BOSS_SELECT));
        fpsText = new TextHandle(new Vector2(WorldVals.PPM, (ViewVals.VIEW_HEIGHT - 1) * WorldVals.PPM),
                () -> "FPS: " + Gdx.graphics.getFramesPerSecond());

        // TODO: get controller codes
        if (ControllerManager.isControllerConnected()) {
            ControllerMapping m = ControllerManager.getController().getMapping();
            logger.log("X: " + m.buttonX);
            logger.log("A: " + m.buttonA);
            logger.log("B: " + m.buttonB);
            logger.log("Y: " + m.buttonY);
            logger.log("L1: " + m.buttonL1);
            logger.log("L2: " + m.buttonL2);
            logger.log("R1: " + m.buttonR1);
            logger.log("R2: " + m.buttonR2);
            logger.log("Start: " + m.buttonStart);
        }
        ctrlMan.setCtrlCode(CtrlBtn.X, 3);
        ctrlMan.setCtrlCode(CtrlBtn.A, 1);
        ctrlMan.setCtrlCode(CtrlBtn.SELECT, 0);
    }

    public boolean isMegamanMaverick() {
        return megaman.isMaverick();
    }

    public void setMegamanMaverick(boolean maverick) {
        megaman.setMaverick(maverick);
    }

    public Screen getScreen(ScreenEnum e) {
        return screens.get(e);
    }

    public void setScreen(ScreenEnum e) {
        setScreen(getScreen(e));
    }

    public <S extends Screen> void setScreen(ScreenEnum e, Class<S> sClass, Consumer<S> sCons) {
        S s = sClass.cast(getScreen(e));
        sCons.accept(s);
        setScreen(s);
    }

    public void setScreen(Screen screen) {
        if (this.screen != null) {
            this.screen.dispose();
        }
        this.screen = screen;
        if (this.screen != null) {
            this.screen.show();
            this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (paused) {
            this.screen.pause();
        }
    }

    public void setOverlayScreen(ScreenEnum e) {
        setOverlayScreen(getScreen(e));
    }

    public <S extends Screen> void setOverlayScreen(ScreenEnum e, Class<S> sClass, Consumer<S> sCons) {
        S s = sClass.cast(getScreen(e));
        sCons.accept(s);
        setOverlayScreen(s);
    }

    public void setOverlayScreen(Screen overlayScreen) {
        if (this.overlayScreen != null) {
            this.overlayScreen.dispose();
        }
        this.overlayScreen = overlayScreen;
        if (this.overlayScreen != null) {
            this.overlayScreen.show();
            this.overlayScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (paused) {
            this.overlayScreen.pause();
        }
    }

    public void removeOverlayScreen() {
        overlayScreen = null;
    }

    @Override
    public void render() {
        Gdx.gl20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        float delta = Gdx.graphics.getDeltaTime();
        ctrlMan.run();
        eventMan.run();
        audioMan.update(delta);
        if (screen != null) {
            screen.render(delta);
        }
        if (overlayScreen != null) {
            overlayScreen.render(delta);
        }
        if (DEBUG) {
            batch.setProjectionMatrix(uiCam.combined);
            batch.begin();
            fpsText.draw(batch);
            batch.end();
        }
        gameViewport.apply();
        uiViewport.apply();
    }

    @Override
    public void pause() {
        logger.log("Game pause method called");
        if (paused) {
            return;
        }
        logger.log("Game pause actuated");
        paused = true;
        if (screen != null) {
            screen.pause();
        }
    }

    @Override
    public void resume() {
        logger.log("Game resume method called");
        if (!paused) {
            return;
        }
        logger.log("Game resume actuated");
        paused = false;
        if (screen != null) {
            screen.resume();
        }
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        uiViewport.update(width, height);
        if (screen != null) {
            screen.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        if (screen != null) {
            screen.dispose();
        }
        batch.dispose();
        assMan.dispose();
        shapeRenderer.dispose();
        gameEngine.getSystem(PathfindingSystem.class).dispose();
    }

}
