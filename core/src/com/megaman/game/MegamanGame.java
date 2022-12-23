package com.megaman.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
import com.megaman.game.events.EventManager;
import com.megaman.game.health.HealthSystem;
import com.megaman.game.movement.pendulum.PendulumSystem;
import com.megaman.game.movement.rotatingline.RotatingLineSystem;
import com.megaman.game.movement.trajectory.TrajectorySystem;
import com.megaman.game.pathfinding.PathfindingSystem;
import com.megaman.game.screens.ScreenEnum;
import com.megaman.game.screens.levels.Level;
import com.megaman.game.screens.levels.LevelScreen;
import com.megaman.game.shapes.LineSystem;
import com.megaman.game.shapes.ShapeSystem;
import com.megaman.game.sprites.SpriteSystem;
import com.megaman.game.updatables.UpdatableSystem;
import com.megaman.game.utils.Logger;
import com.megaman.game.world.WorldContactListener;
import com.megaman.game.world.WorldContactListenerImpl;
import com.megaman.game.world.WorldSystem;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

@Getter
public class MegamanGame extends Game {

    public static final boolean DEBUG = true;

    private static final Logger logger = new Logger(MegamanGame.class, DEBUG);

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Map<ScreenEnum, Screen> screens;

    private AssetsManager assMan;
    private AudioManager audioMan;
    private EventManager eventMan;
    private ControllerManager ctrlMan;

    private Megaman megaman;
    private GameEngine gameEngine;
    private EntityFactories entityFactories;

    private boolean debug;

    @Override
    public void create() {
        debug = false;
        // renderers
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        // managers
        ctrlMan = new ControllerManager();
        audioMan = new AudioManager();
        assMan = new AssetsManager();
        assMan.loadAssets();
        // events
        eventMan = new EventManager();
        // entity factories
        entityFactories = new EntityFactories(this);
        // game engine
        WorldContactListener w = new WorldContactListenerImpl(this);
        gameEngine = new GameEngine(
                new ControllerSystem(ctrlMan),
                new CullOnOutOfBoundsSystem(),
                new CullOnEventSystem(eventMan),
                new HealthSystem(),
                new TrajectorySystem(),
                new WorldSystem(w),
                new PathfindingSystem(),
                new RotatingLineSystem(),
                new PendulumSystem(),
                new UpdatableSystem(),
                new BehaviorSystem(),
                new AnimationSystem(),
                new SpriteSystem(),
                new LineSystem(),
                new ShapeSystem(),
                new SoundSystem(assMan, audioMan));
        // megaman
        megaman = new Megaman(this);
        // put screens
        screens = new EnumMap<>(ScreenEnum.class);
        screens.put(ScreenEnum.LEVEL, new LevelScreen(this));
        // set screen
        setLevelScreen(Level.TEST);
    }

    public void setLevelScreen(Level level) {
        LevelScreen levelScreen = (LevelScreen) screens.get(ScreenEnum.LEVEL);
        levelScreen.set(level.getTmxFile());
        setScreen(levelScreen);
        if (level.getMusicAsset() != null) {
            levelScreen.setMusic(level.getMusicAsset());
            levelScreen.playMusic(true);
        }
    }

    @Override
    public void setScreen(Screen screen) {
        if (this.screen != null) {
            this.screen.dispose();
        }
        this.screen = screen;
        if (this.screen != null) {
            this.screen.show();
            this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    @Override
    public void render() {
        Gdx.gl20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        ctrlMan.update();
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        assMan.dispose();
        screen.dispose();
        shapeRenderer.dispose();
        gameEngine.getSystem(PathfindingSystem.class).dispose();
    }

}
