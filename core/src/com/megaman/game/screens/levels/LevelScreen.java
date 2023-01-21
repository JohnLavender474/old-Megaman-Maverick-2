package com.megaman.game.screens.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.AnimationSystem;
import com.megaman.game.assets.MusicAsset;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.audio.SoundSystem;
import com.megaman.game.backgrounds.Background;
import com.megaman.game.behaviors.BehaviorSystem;
import com.megaman.game.controllers.CtrlBtn;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.controllers.ControllerSystem;
import com.megaman.game.cull.CullOnOutOfBoundsSystem;
import com.megaman.game.entities.impl.megaman.Megaman;
import com.megaman.game.entities.impl.megaman.upgrades.MegaHeartTank;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.events.EventManager;
import com.megaman.game.events.EventType;
import com.megaman.game.movement.trajectory.TrajectorySystem;
import com.megaman.game.pathfinding.PathfindingSystem;
import com.megaman.game.screens.levels.camera.CamManager;
import com.megaman.game.screens.levels.camera.CamShaker;
import com.megaman.game.screens.levels.handlers.player.PlayerDeathEventHandler;
import com.megaman.game.screens.levels.handlers.player.PlayerSpawnEventHandler;
import com.megaman.game.screens.levels.handlers.player.PlayerStatsHandler;
import com.megaman.game.screens.levels.handlers.state.LevelStateHandler;
import com.megaman.game.screens.levels.map.LevelMapLayer;
import com.megaman.game.screens.levels.map.LevelMapManager;
import com.megaman.game.screens.levels.spawns.SpawnManager;
import com.megaman.game.screens.levels.spawns.player.PlayerSpawnManager;
import com.megaman.game.shapes.LineSystem;
import com.megaman.game.shapes.RenderableShape;
import com.megaman.game.shapes.ShapeSystem;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.sprites.SpriteSystem;
import com.megaman.game.updatables.UpdatableSystem;
import com.megaman.game.utils.ConstFuncs;
import com.megaman.game.utils.Logger;
import com.megaman.game.world.BodyComponent;
import com.megaman.game.world.WorldGraph;
import com.megaman.game.world.WorldSystem;
import com.megaman.game.world.WorldVals;
import lombok.Setter;

import java.util.Map;
import java.util.PriorityQueue;

public class LevelScreen extends ScreenAdapter implements EventListener {

    private static final Logger logger = new Logger(LevelScreen.class, MegamanGame.DEBUG && true);

    private final MegamanGame game;

    private final Megaman megaman;
    private final GameEngine engine;
    private final EventManager eventMan;
    private final AudioManager audioMan;
    private final ControllerManager ctrlMan;

    private final OrthographicCamera uiCam;
    private final OrthographicCamera gameCam;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    private final LevelMapManager levelMapMan;
    private final CamManager levelCamMan;
    private final CamShaker camShaker;

    private final Array<Background> backgrounds;
    private final PriorityQueue<SpriteHandle> gameSpritesQ;
    private final PriorityQueue<RenderableShape> gameShapesQ;

    private final SpawnManager spawnMan;
    private final PlayerSpawnManager playerSpawnMan;

    private final LevelStateHandler stateHandler;
    private final PlayerStatsHandler playerStatsHandler;
    private final PlayerSpawnEventHandler playerSpawnEventHandler;
    private final PlayerDeathEventHandler playerDeathEventHandler;

    private final Array<Disposable> disposables;

    @Setter
    private MusicAsset music;

    public LevelScreen(MegamanGame game) {
        this.game = game;
        batch = game.getBatch();
        uiCam = game.getUiCam();
        gameCam = game.getGameCam();
        megaman = game.getMegaman();
        backgrounds = new Array<>();
        disposables = new Array<>();
        ctrlMan = game.getCtrlMan();
        eventMan = game.getEventMan();
        audioMan = game.getAudioMan();
        engine = game.getGameEngine();
        gameShapesQ = new PriorityQueue<>();
        gameSpritesQ = new PriorityQueue<>();
        shapeRenderer = game.getShapeRenderer();
        stateHandler = new LevelStateHandler(game);
        playerSpawnMan = new PlayerSpawnManager(gameCam);
        playerStatsHandler = new PlayerStatsHandler(game);
        playerSpawnEventHandler = new PlayerSpawnEventHandler(game);
        playerDeathEventHandler = new PlayerDeathEventHandler(game);
        levelCamMan = new CamManager(gameCam);
        levelCamMan.setRunOnBeginTrans(() -> {
            engine.set(false,
                    AnimationSystem.class,
                    ControllerSystem.class,
                    TrajectorySystem.class,
                    UpdatableSystem.class,
                    BehaviorSystem.class,
                    WorldSystem.class,
                    SoundSystem.class);
            eventMan.submit(new Event(EventType.BEGIN_ROOM_TRANS, new ObjectMap<>() {{
                put(ConstKeys.POS, levelCamMan.getTransInterpolation());
                put(ConstKeys.CURR, levelCamMan.getCurrGameRoom());
                put(ConstKeys.PRIOR, levelCamMan.getPriorGameRoom());
            }}));
            ShapeUtils.setBottomCenterToPoint(megaman.body.bounds, levelCamMan.getTransInterpolation());
        });
        levelCamMan.setUpdateOnTrans(delta -> {
            if (levelCamMan.isDelayJustFinished()) {
                engine.set(true, AnimationSystem.class);
            }
            eventMan.submit(new Event(EventType.CONTINUE_ROOM_TRANS, new ObjectMap<>() {{
                put(ConstKeys.POS, levelCamMan.getTransInterpolation());
            }}));
            ShapeUtils.setBottomCenterToPoint(megaman.body.bounds, levelCamMan.getTransInterpolation());
        });
        levelCamMan.setRunOnEndTrans(() -> {
            engine.set(true,
                    ControllerSystem.class,
                    TrajectorySystem.class,
                    UpdatableSystem.class,
                    BehaviorSystem.class,
                    WorldSystem.class,
                    SoundSystem.class);
            eventMan.submit(new Event(EventType.END_ROOM_TRANS, new ObjectMap<>() {{
                put(ConstKeys.ROOM, levelCamMan.getCurrGameRoom());
            }}));
            if (levelCamMan.getCurrGameRoom().getName().equals(ConstKeys.BOSS)) {
                eventMan.submit(new Event(EventType.ENTER_BOSS_ROOM));
            }
        });
        camShaker = new CamShaker(gameCam);
        levelMapMan = new LevelMapManager(gameCam, game.getBatch());
        spawnMan = new SpawnManager();
    }

    public void set(Level level) {
        dispose();
        if (level.getMusicAss() != null) {
            setMusic(level.getMusicAss());
        }
        uiCam.position.set(ConstFuncs.getCamInitPos());
        gameCam.position.set(ConstFuncs.getCamInitPos());
        engine.setAll(true);
        engine.getSystem(SpriteSystem.class).set(gameCam, gameSpritesQ);
        engine.getSystem(LineSystem.class).setGameShapesQ(gameShapesQ);
        engine.getSystem(ShapeSystem.class).setGameShapesQ(gameShapesQ);
        engine.getSystem(CullOnOutOfBoundsSystem.class).setGameCam(gameCam);
        Map<LevelMapLayer, Array<RectangleMapObject>> m = levelMapMan.set(level.getTmxFile());
        WorldGraph worldGraph = new WorldGraph(levelMapMan.getWorldWidth(), levelMapMan.getWorldHeight());
        engine.getSystem(WorldSystem.class).setWorldGraph(worldGraph);
        engine.getSystem(PathfindingSystem.class).setWorldGraph(worldGraph);
        LevelBuilder builder = new LevelBuilder(game, m);
        backgrounds.clear();
        backgrounds.addAll(builder.getBackgrounds());
        levelCamMan.set(builder.getGameRoomObjs(), megaman);
        playerSpawnMan.set(builder.getPlayerSpawns());
        spawnMan.set(builder.getSpawns());
        disposables.add(builder);
    }

    @Override
    public void listenForEvent(Event e) {
        switch (e.type) {
            case GAME_PAUSE -> pause();
            case GAME_RESUME -> resume();
            case PLAYER_SPAWN -> {
                levelCamMan.reset();
                engine.spawn(megaman, playerSpawnMan.getSpawn(), playerSpawnMan.getData());
            }
            case PLAYER_JUST_DIED -> {
                audioMan.clearMusic();
                playerDeathEventHandler.init();
            }
            case PLAYER_DONE_DYIN -> {
                audioMan.play(music, true);
                playerSpawnEventHandler.init();
            }
            case ADD_PLAYER_HEALTH -> {
                int healthNeeded = megaman.maxHealth - megaman.getHealth();
                if (healthNeeded > 0) {
                    int health = e.getInfo(ConstKeys.VAL, Integer.class);
                    playerStatsHandler.addHealth(health);
                }
            }
            case ADD_HEART_TANK -> {
                MegaHeartTank h = e.getInfo(ConstKeys.VAL, MegaHeartTank.class);
                if (megaman.has(h)) {
                    break;
                }
                playerStatsHandler.attain(h);
            }
            case GATE_INIT_OPENING -> {
                engine.set(false,
                        ControllerSystem.class,
                        TrajectorySystem.class,
                        BehaviorSystem.class,
                        WorldSystem.class);
                megaman.getComponent(BodyComponent.class).body.velocity.setZero();
            }
            case NEXT_ROOM_REQ -> levelCamMan.transToRoom(e.getInfo(ConstKeys.ROOM, String.class));
            case GATE_INIT_CLOSING -> engine.set(true,
                    ControllerSystem.class,
                    TrajectorySystem.class,
                    BehaviorSystem.class,
                    WorldSystem.class);
            case REQ_SHAKE_CAM -> {
                float dur = e.getInfo(ConstKeys.DUR, Float.class);
                float interval = e.getInfo(ConstKeys.INTERVAL, Float.class);
                float shakeX = e.hasInfo(ConstKeys.X) ? e.getInfo(ConstKeys.X, Float.class) : 0f;
                float shakeY = e.hasInfo(ConstKeys.Y) ? e.getInfo(ConstKeys.Y, Float.class) : 0f;
                camShaker.startShake(dur, interval, shakeX, shakeY);
            }
        }
    }

    @Override
    public void show() {
        eventMan.add(this);
        if (music != null) {
            audioMan.set(music, true);
        }
        playerSpawnEventHandler.init();
    }

    @Override
    public void render(float delta) {
        // TODO: test cam shaker
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            eventMan.submit(new Event(EventType.REQ_SHAKE_CAM, new ObjectMap<>() {{
                put(ConstKeys.DUR, 1f);
                put(ConstKeys.INTERVAL, .1f);
                put(ConstKeys.Y, .1f * WorldVals.PPM);
            }}));
        }

        // game can only be paused if neither spawn nor death events are occurring
        if (ctrlMan.isJustPressed(CtrlBtn.START) &&
                playerSpawnEventHandler.isFinished() &&
                playerDeathEventHandler.isFinished() &&
                playerStatsHandler.isFinished()) {
            if (game.isPaused()) {
                game.resume();
            } else {
                game.pause();
            }
        }

        // illegal for game to be paused when a handler is not finished, force resume
        if (game.isPaused() && (!playerDeathEventHandler.isFinished() ||
                !playerSpawnEventHandler.isFinished() ||
                !playerStatsHandler.isFinished())) {
            game.resume();
        }

        // update only if game is not paused
        if (!game.isPaused()) {
            for (Background b : backgrounds) {
                b.update(delta);
            }
            levelCamMan.update(delta);
            // spawns do not update when player is first spawning or if there is room transition
            if (playerSpawnEventHandler.isFinished() && !levelCamMan.isTransitioning()) {
                playerSpawnMan.run();
                spawnMan.update(delta);
            }
            // only update one handler at a time
            if (!playerSpawnEventHandler.isFinished()) {
                playerSpawnEventHandler.update(delta);
            } else if (!playerDeathEventHandler.isFinished()) {
                playerDeathEventHandler.update(delta);
            } else if (!playerStatsHandler.isFinished()) {
                playerStatsHandler.update(delta);
            }
        }

        // update engine
        engine.update(delta);

        // render game sprites
        batch.setProjectionMatrix(gameCam.combined);
        batch.begin();
        for (Background b : backgrounds) {
            b.draw(batch);
        }
        levelMapMan.draw();
        while (!gameSpritesQ.isEmpty()) {
            gameSpritesQ.poll().draw(batch);
        }
        batch.end();

        // render ui
        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        playerStatsHandler.draw(batch);
        if (!playerSpawnEventHandler.isFinished()) {
            playerSpawnEventHandler.draw(batch);
        }
        batch.end();

        // render shapes
        shapeRenderer.setProjectionMatrix(gameCam.combined);
        shapeRenderer.begin();
        while (!gameShapesQ.isEmpty()) {
            gameShapesQ.poll().render(shapeRenderer);
        }
        shapeRenderer.end();

        // shake cam if shaking
        if (!camShaker.isFinished()) {
            camShaker.update(delta);
        }
    }

    @Override
    public void pause() {
        logger.log("Level screen pause method called");
        stateHandler.pause();
    }

    @Override
    public void resume() {
        logger.log("Level screen resume method called");
        stateHandler.resume();
    }

    @Override
    public void dispose() {
        engine.reset();
        spawnMan.reset();
        audioMan.stopMusic();
        eventMan.remove(this);
        levelMapMan.dispose();
        playerSpawnMan.reset();
        for (Disposable d : disposables) {
            d.dispose();
        }
    }

}
