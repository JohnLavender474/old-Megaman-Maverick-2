package com.megaman.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.megaman.game.animations.AnimationSystem;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.audio.SoundSystem;
import com.megaman.game.behaviors.BehaviorSystem;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.controllers.ControllerSystem;
import com.megaman.game.cull.CullOnEventSystem;
import com.megaman.game.cull.CullOnOutOfBoundsSystem;
import com.megaman.game.entities.EntityFactories;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.events.EventListenerSystem;
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
import com.megaman.game.shapes.LineSystem;
import com.megaman.game.shapes.ShapeSystem;
import com.megaman.game.sprites.SpriteSystem;
import com.megaman.game.updatables.UpdatableSystem;
import com.megaman.game.utils.Logger;
import com.megaman.game.world.WorldContactListenerImpl;
import com.megaman.game.world.WorldSystem;
import com.megaman.game.world.WorldVals;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

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
    private boolean paused;
    private Screen currScreen;

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
                new WorldSystem(new WorldContactListenerImpl(this)),
                new EventListenerSystem(eventMan),
                new CullOnEventSystem(eventMan),
                new CullOnOutOfBoundsSystem(),
                new TrajectorySystem(),
                new PathfindingSystem(),
                new RotatingLineSystem(),
                new PendulumSystem(),
                new HealthSystem(),
                new UpdatableSystem(),
                new BehaviorSystem(),
                new AnimationSystem(),
                new SpriteSystem(),
                new LineSystem(),
                new ShapeSystem(),
                new SoundSystem(audioMan));
        megaman = new Megaman(this);
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

        // TODO: test levels
        LevelScreen l = getScreen(ScreenEnum.LEVEL, LevelScreen.class);
        l.set(Level.TEST1);
        setCurrScreen(l);

        // TODO: set to main menu
        // setCurrScreen(getScreen(ScreenEnum.MAIN));
    }

    public <S extends Screen> S getScreen(ScreenEnum e, Class<S> sClass) {
        return sClass.cast(getScreen(e));
    }

    public Screen getScreen(ScreenEnum e) {
        return screens.get(e);
    }

    public void setCurrScreen(Screen screen) {
        if (currScreen != null) {
            currScreen.dispose();
        }
        currScreen = screen;
        if (currScreen != null) {
            currScreen.show();
            currScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        if (paused) {
            currScreen.pause();
        }
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
        audioMan.update(delta);
        if (currScreen != null) {
            currScreen.render(delta);
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
        if (currScreen != null) {
            currScreen.pause();
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
        if (currScreen != null) {
            currScreen.resume();
        }
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        uiViewport.update(width, height);
        if (currScreen != null) {
            currScreen.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        if (currScreen != null) {
            currScreen.dispose();
        }
        batch.dispose();
        assMan.dispose();
        shapeRenderer.dispose();
        gameEngine.getSystem(PathfindingSystem.class).dispose();
    }

}
