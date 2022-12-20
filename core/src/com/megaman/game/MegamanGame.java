package com.megaman.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
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
import com.megaman.game.screens.ScreenEnum;
import com.megaman.game.screens.levels.Level;
import com.megaman.game.screens.levels.LevelScreen;
import com.megaman.game.shapes.LineSystem;
import com.megaman.game.shapes.ShapeSystem;
import com.megaman.game.sprites.SpriteSystem;
import com.megaman.game.updatables.UpdatableSystem;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.world.WorldContactListenerImpl;
import com.megaman.game.world.WorldSystem;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;

@Getter
public class MegamanGame extends Game {

    private final Map<ScreenEnum, Screen> screens = new EnumMap<>(ScreenEnum.class);
    private final Array<Disposable> disposables = new Array<>();

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private Megaman megaman;

    private AssetsManager assMan;
    private AudioManager audioMan;
    private EventManager eventMan;
    private ControllerManager ctrlMan;

    private GameEngine gameEngine;
    private EntityFactories entityFactories;

    private boolean debug;

    @Override
    public void create() {
        debug = false;
        // renderers
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        // managers
        ctrlMan = new ControllerManager();
        assMan = new AssetsManager();
        assMan.loadAssets();
        audioMan = new AudioManager();
        // events
        eventMan = new EventManager();
        // disposables
        disposables.add(batch);
        disposables.add(assMan);
        disposables.add(shapeRenderer);
        // entity factories
        entityFactories = new EntityFactories(this);
        // game engine
        gameEngine = new GameEngine(new Array<>() {{
            add(new ControllerSystem(ctrlMan));
            add(new CullOnOutOfBoundsSystem());
            add(new CullOnEventSystem(eventMan));
            add(new HealthSystem());
            // TODO: trajectory system
            add(new WorldSystem(new WorldContactListenerImpl(MegamanGame.this)));
            // TODO: graph system
            // TODO: pathfinding system
            // TODO: rotating line system
            // TODO: pendulum system
            add(new UpdatableSystem());
            add(new BehaviorSystem());
            add(new AnimationSystem());
            add(new SpriteSystem());
            add(new LineSystem());
            add(new ShapeSystem());
            add(new SoundSystem(assMan, audioMan));
        }});
        // megaman
        megaman = new Megaman(this);
    }

    public void setLevelScreen(Level level) {
        LevelScreen levelScreen = (LevelScreen) screens.get(ScreenEnum.LEVEL);
        levelScreen.set(level.tmxFile);
        setScreen(levelScreen);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        screen.dispose();
        for (Disposable d : disposables) {
            d.dispose();
        }
    }

}
