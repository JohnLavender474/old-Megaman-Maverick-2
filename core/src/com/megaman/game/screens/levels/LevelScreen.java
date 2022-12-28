package com.megaman.game.screens.levels;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.System;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.MusicAsset;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.audio.SoundSystem;
import com.megaman.game.backgrounds.Background;
import com.megaman.game.behaviors.BehaviorSystem;
import com.megaman.game.controllers.ControllerBtn;
import com.megaman.game.controllers.ControllerSystem;
import com.megaman.game.cull.CullOnOutOfBoundsSystem;
import com.megaman.game.entities.EntityFactories;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.megaman.weapons.MegamanWeapon;
import com.megaman.game.entities.sensors.SensorFactory;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.events.EventType;
import com.megaman.game.movement.trajectory.TrajectorySystem;
import com.megaman.game.pathfinding.PathfindingSystem;
import com.megaman.game.screens.levels.camera.LevelCamManager;
import com.megaman.game.screens.levels.map.LevelMapLayer;
import com.megaman.game.screens.levels.map.LevelMapManager;
import com.megaman.game.screens.levels.map.LevelMapObjParser;
import com.megaman.game.screens.levels.spawns.Spawn;
import com.megaman.game.screens.levels.spawns.SpawnManager;
import com.megaman.game.screens.levels.spawns.SpawnType;
import com.megaman.game.screens.levels.spawns.impl.SpawnWhenInBounds;
import com.megaman.game.screens.levels.spawns.player.PlayerSpawnManager;
import com.megaman.game.screens.utils.BitsBar;
import com.megaman.game.screens.utils.TextHandle;
import com.megaman.game.shapes.LineSystem;
import com.megaman.game.shapes.RenderableShape;
import com.megaman.game.shapes.ShapeSystem;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteDrawer;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.sprites.SpriteSystem;
import com.megaman.game.updatables.UpdatableSystem;
import com.megaman.game.utils.ConstFuncs;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.objs.KeyValuePair;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.BodyComponent;
import com.megaman.game.world.WorldGraph;
import com.megaman.game.world.WorldSystem;
import com.megaman.game.world.WorldVals;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Supplier;

public class LevelScreen extends ScreenAdapter implements EventListener {

    private static final Logger logger = new Logger(LevelScreen.class, MegamanGame.DEBUG && true);

    private static final float ON_PLAYER_DEATH_DELAY = 4f;
    private static final float ENTER_BOSS_ROOM_DELAY = .5f;
    private static final float BOSS_INTRO_DUR = 2f;
    private static final float READY_DUR = 1f;

    private final MegamanGame game;
    private final OrthographicCamera uiCam;
    private final OrthographicCamera gameCam;
    private final LevelMapManager levelMapMan;
    private final LevelCamManager levelCamMan;
    private final SpawnManager spawnMan;
    private final PlayerSpawnManager playerSpawnMan;
    private final Array<Runnable> runOnDispose;
    private final Timer playerDeathTimer;
    private final Timer readyTimer;
    private final Timer enterBossRoomTimer;
    private final Timer bossIntroTimer;
    private final Array<Background> backgrounds;
    private final PriorityQueue<SpriteHandle> gameSpritesQ;
    private final PriorityQueue<RenderableShape> gameShapesQ;
    private final Queue<TextHandle> uiText;
    private final Array<KeyValuePair<Supplier<Boolean>, Drawable>> uiDrawables;

    private OrderedMap<Class<? extends System>, Boolean> sysStatesOnPause;
    private boolean playerDead;
    private boolean playerReady;
    private boolean set;
    @Setter
    private MusicAsset music;

    public LevelScreen(MegamanGame game) {
        this.game = game;
        playerReady = true;
        runOnDispose = new Array<>();
        playerDeathTimer = new Timer(ON_PLAYER_DEATH_DELAY, true);
        enterBossRoomTimer = new Timer(ENTER_BOSS_ROOM_DELAY, true);

        // TODO: flash "READY" text
        readyTimer = new Timer(READY_DUR, true);

        bossIntroTimer = new Timer(BOSS_INTRO_DUR, true, new Array<>() {{

            // TODO: temp override int supplier in boss health bar
            // TODO: runnables for filling up boss health bar

        }});
        backgrounds = new Array<>();
        gameSpritesQ = new PriorityQueue<>();
        gameShapesQ = new PriorityQueue<>();
        uiText = new LinkedList<>();
        uiCam = game.getUiCam();
        uiDrawables = new Array<>();
        gameCam = game.getGameCam();
        spawnMan = new SpawnManager();
        playerSpawnMan = new PlayerSpawnManager();
        levelCamMan = new LevelCamManager(gameCam);
        levelCamMan.setRunOnBeginTrans(() -> {
            game.getGameEngine().setSystemsOn(false,
                    ControllerSystem.class,
                    TrajectorySystem.class,
                    UpdatableSystem.class,
                    BehaviorSystem.class,
                    WorldSystem.class,
                    SoundSystem.class);
            game.getEventMan().submitEvent(new Event(EventType.BEGIN_GAME_ROOM_TRANS, new ObjectMap<>() {{
                put(ConstKeys.POS, levelCamMan.getTransInterpolation());
                put(ConstKeys.CURR, levelCamMan.getCurrGameRoom());
                put(ConstKeys.PRIOR, levelCamMan.getPriorGameRoom());
            }}));
            ShapeUtils.setBottomCenterToPoint(game.getMegaman().body.bounds, levelCamMan.getTransInterpolation());
        });
        levelCamMan.setUpdateOnTrans(delta -> {
            game.getEventMan().submitEvent(new Event(EventType.CONTINUE_GAME_ROOM_TRANS, new ObjectMap<>() {{
                put(ConstKeys.POS, levelCamMan.getTransInterpolation());
            }}));
            ShapeUtils.setBottomCenterToPoint(game.getMegaman().body.bounds, levelCamMan.getTransInterpolation());
        });
        levelCamMan.setRunOnEndTrans(() -> {
            game.getGameEngine().setSystemsOn(true,
                    ControllerSystem.class,
                    TrajectorySystem.class,
                    UpdatableSystem.class,
                    BehaviorSystem.class,
                    WorldSystem.class,
                    SoundSystem.class);
            game.getEventMan().submitEvent(new Event(EventType.END_GAME_ROOM_TRANS, new ObjectMap<>() {{
                put(ConstKeys.ROOM, levelCamMan.getCurrGameRoom());
            }}));
            if (levelCamMan.getCurrGameRoom().getName().equals(ConstKeys.BOSS)) {
                game.getEventMan().submitEvent(new Event(EventType.ENTER_BOSS_ROOM));
            }
        });
        levelMapMan = new LevelMapManager(gameCam, game.getBatch());
        AssetsManager assMan = game.getAssMan();
        Megaman megaman = game.getMegaman();
        float healthBarX = WorldVals.PPM * .4f;
        float healthBarY = WorldVals.PPM * 9f;
        BitsBar healthBar = new BitsBar(healthBarX, healthBarY, megaman::getHealth, assMan, "StandardBit");
        addUiDrawable(healthBar);
        for (MegamanWeapon weapon : MegamanWeapon.values()) {
            if (weapon == MegamanWeapon.MEGA_BUSTER) {
                continue;
            }
            BitsBar weaponBar = new BitsBar(healthBarX + WorldVals.PPM, healthBarY,
                    megaman::getCurrentAmmo, assMan, weapon.weaponBitSrc);
            addUiDrawable(() -> weapon == megaman.currWeapon, weaponBar);
        }
    }

    public boolean isPlayerDeathEvent() {
        return !playerDeathTimer.isFinished();
    }

    public void set(Level level) {
        dispose();
        if (level.getMusicAss() != null) {
            setMusic(level.getMusicAss());
        }
        playerDeathTimer.setToEnd();
        enterBossRoomTimer.setToEnd();
        bossIntroTimer.setToEnd();
        uiCam.position.set(ConstFuncs.getCamInitPos());
        gameCam.position.set(ConstFuncs.getCamInitPos());
        GameEngine engine = game.getGameEngine();
        engine.setAllSystemsOn(true);
        engine.getSystem(SpriteSystem.class).set(gameCam, gameSpritesQ);
        engine.getSystem(LineSystem.class).setGameShapesQ(gameShapesQ);
        engine.getSystem(ShapeSystem.class).setGameShapesQ(gameShapesQ);
        engine.getSystem(CullOnOutOfBoundsSystem.class).setGameCam(gameCam);
        Map<LevelMapLayer, Array<RectangleMapObject>> m = levelMapMan.set(level.getTmxFile());
        WorldGraph worldGraph = new WorldGraph(levelMapMan.getWorldWidth(), levelMapMan.getWorldHeight());
        engine.getSystem(WorldSystem.class).setWorldGraph(worldGraph);
        engine.getSystem(PathfindingSystem.class).setWorldGraph(worldGraph);
        Array<RectangleMapObject> gameRoomsObjs = m.get(LevelMapLayer.GAME_ROOMS);
        levelCamMan.set(gameRoomsObjs, game.getMegaman());
        Array<RectangleMapObject> playerSpawns = m.get(LevelMapLayer.PLAYER_SPAWNS);
        playerSpawnMan.set(gameCam, playerSpawns);
        Array<Spawn> spawns = new Array<>();
        EntityFactories factories = game.getEntityFactories();
        for (Map.Entry<LevelMapLayer, Array<RectangleMapObject>> e : m.entrySet()) {
            switch (e.getKey()) {
                case GATES -> {
                    for (RectangleMapObject o : e.getValue()) {
                        engine.spawnEntity(factories.fetch(EntityType.SENSOR, SensorFactory.GATE),
                                o.getRectangle(), LevelMapObjParser.parse(o));
                    }
                }
                case DEATH -> {
                    for (RectangleMapObject o : e.getValue()) {
                        engine.spawnEntity(factories.fetch(EntityType.SENSOR, SensorFactory.DEATH), o.getRectangle());
                    }
                }
                case ENEMY_SPAWNS, BLOCKS, HAZARDS, SPECIAL -> {
                    EntityType type = switch (e.getKey()) {
                        case BLOCKS -> EntityType.BLOCK;
                        case HAZARDS -> EntityType.HAZARD;
                        case SPECIAL -> EntityType.SPECIAL;
                        case ENEMY_SPAWNS -> EntityType.ENEMY;
                        default -> throw new IllegalStateException("No matching entity type for: " + e.getKey());
                    };
                    for (RectangleMapObject o : e.getValue()) {
                        ObjectMap<String, Object> data = LevelMapObjParser.parse(o);
                        if (data.containsKey(SpawnType.SPAWN_TYPE)) {
                            String spawnType = (String) data.get(SpawnType.SPAWN_TYPE);
                            switch (spawnType) {
                                case SpawnType.SPAWN_ROOM -> {
                                    String roomName = (String) data.get(SpawnType.SPAWN_ROOM);
                                    boolean roomFound = false;
                                    for (RectangleMapObject room : gameRoomsObjs) {
                                        if (roomName.equals(room.getName())) {
                                            data.put(ConstKeys.SPAWN, o.getRectangle());
                                            data.put(ConstKeys.ROOM, room.getRectangle());
                                            spawns.add(new SpawnWhenInBounds(
                                                    engine,
                                                    gameCam,
                                                    room.getRectangle(),
                                                    data,
                                                    () -> factories.fetch(type, o.getName())));
                                            roomFound = true;
                                            break;
                                        }
                                    }
                                    if (!roomFound) {
                                        throw new IllegalStateException(
                                                "Failed to create spawn for room: " + roomName);
                                    }
                                }
                                case SpawnType.SPAWN_EVENT -> {

                                    // TODO: create spawn by event

                                }
                            }
                        } else {
                            spawns.add(new SpawnWhenInBounds(
                                    engine,
                                    gameCam,
                                    o.getRectangle(),
                                    data,
                                    () -> factories.fetch(type, o.getName())));
                        }
                    }
                }
            }
        }
        spawnMan.set(spawns);
        set = true;
    }

    @Override
    public void listenForEvent(Event e) {
        GameEngine engine = game.getGameEngine();
        AudioManager audioMan = game.getAudioMan();
        switch (e.type) {
            case GAME_PAUSE -> pause();
            case GAME_RESUME -> resume();
            case PLAYER_SPAWN -> {
                playerDead = false;
                // TODO: reset ready timer, set megaman.ready to false
            }
            case PLAYER_READY -> {
                playerReady = true;
                engine.setSystemsOn(true, ControllerSystem.class);
                // TODO: on ready timer is finished, set megaman.ready to true
            }
            case PLAYER_DEAD -> {
                playerDead = true;
                playerReady = false;
                playerDeathTimer.reset();
                audioMan.stopMusic();
                audioMan.playSound(SoundAsset.MEGAMAN_DEFEAT_SOUND);
                engine.setSystemsOn(false, ControllerSystem.class);
            }
            case GATE_INIT_OPENING -> {
                engine.setSystemsOn(false,
                        ControllerSystem.class,
                        TrajectorySystem.class,
                        BehaviorSystem.class,
                        WorldSystem.class);
                game.getMegaman().getComponent(BodyComponent.class).body.velocity.setZero();
            }
            case NEXT_GAME_ROOM_REQ -> levelCamMan.transToRoom(e.getInfo(ConstKeys.ROOM, String.class));
            case ENTER_BOSS_ROOM -> {
                enterBossRoomTimer.reset();
                engine.setSystemsOn(false, ControllerSystem.class);
            }
            case GATE_INIT_CLOSING -> engine.setSystemsOn(true,
                    ControllerSystem.class,
                    TrajectorySystem.class,
                    BehaviorSystem.class,
                    WorldSystem.class);
            case BOSS_DROP_DOWN -> {
                bossIntroTimer.reset();
                // TODO: play boss intro music
            }
        }
    }

    @Override
    public void show() {
        spawnMegaman();
        game.getEventMan().add(this);
        if (music != null) {
            game.getAudioMan().playMusic(music, true);
        }
    }

    @Override
    public void render(float delta) {
        if (!set) {
            throw new IllegalStateException("Must call set method before rendering");
        }
        if (game.getCtrlMan().isJustPressed(ControllerBtn.START)) {
            if (game.isPaused()) {
                game.resume();
            } else {
                game.pause();
            }
        }
        if (!game.isPaused()) {
            for (Background b : backgrounds) {
                b.update(delta);
            }
            levelCamMan.update(delta);
            if (!playerDead && playerReady && levelCamMan.getTransState() == null) {
                playerSpawnMan.run();
                spawnMan.update(delta);
            }

            // TODO: test new process
            /*
            else {
                switch (levelCamMan.getTransState()) {
                    case BEGIN -> {
                        engine.setSystemsOn(false,
                                ControllerSystem.class,
                                TrajectorySystem.class,
                                UpdatableSystem.class,
                                BehaviorSystem.class,
                                WorldSystem.class,
                                SoundSystem.class);
                        eventMan.submitEvent(new Event(EventType.BEGIN_GAME_ROOM_TRANS, new ObjectMap<>() {{
                            put(ConstKeys.POS, levelCamMan.getTransInterpolation());
                            put(ConstKeys.NEXT, levelCamMan.getCurrGameRoom());
                            put(ConstKeys.PRIOR, levelCamMan.getPriorGameRoom());
                        }}));
                        ShapeUtils.setBottomCenterToPoint(megaman.body.bounds, levelCamMan.getTransInterpolation());
                    }
                    case CONTINUE -> {
                        eventMan.submitEvent(new Event(EventType.CONTINUE_GAME_ROOM_TRANS, new ObjectMap<>() {{
                            put(ConstKeys.POS, levelCamMan.getTransInterpolation());
                        }}));
                        ShapeUtils.setBottomCenterToPoint(megaman.body.bounds, levelCamMan.getTransInterpolation());
                    }
                    case END -> {
                        engine.setSystemsOn(true,
                                ControllerSystem.class,
                                TrajectorySystem.class,
                                UpdatableSystem.class,
                                BehaviorSystem.class,
                                WorldSystem.class,
                                SoundSystem.class);
                        eventMan.submitEvent(new Event(EventType.END_GAME_ROOM_TRANS, new ObjectMap<>() {{
                            put(ConstKeys.ROOM, levelCamMan.getCurrGameRoom());
                        }}));
                        if (levelCamMan.getCurrGameRoom().getName().equals(ConstKeys.BOSS)) {
                            eventMan.submitEvent(new Event(EventType.ENTER_BOSS_ROOM));
                        }
                    }
                }
            }
             */
            enterBossRoomTimer.update(delta);
            if (enterBossRoomTimer.isJustFinished()) {
                game.getEventMan().submitEvent(new Event(EventType.BOSS_DROP_DOWN));
            }
            playerDeathTimer.update(delta);
            if (playerDeathTimer.isJustFinished()) {
                if (music != null) {
                    game.getAudioMan().playMusic(music, true);
                }
                readyTimer.reset();
                spawnMegaman();
                // TODO: test ready timer
                // spawnMegaman();
            }
            readyTimer.update(delta);
            if (readyTimer.isJustFinished()) {
                game.getEventMan().submitEvent(new Event(EventType.PLAYER_READY));
            }
        }
        game.getGameEngine().update(delta);
        SpriteBatch batch = game.getBatch();
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
        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        for (KeyValuePair<Supplier<Boolean>, Drawable> uiDrawable : uiDrawables) {
            if (!uiDrawable.key().get()) {
                continue;
            }
            if (uiDrawable.value() instanceof Sprite s) {
                SpriteDrawer.draw(s, batch);
            } else {
                uiDrawable.value().draw(batch);
            }
        }
        while (!uiText.isEmpty()) {
            uiText.poll().draw(batch);
        }
        batch.end();
        ShapeRenderer shapeRenderer = game.getShapeRenderer();
        shapeRenderer.setProjectionMatrix(gameCam.combined);
        shapeRenderer.begin();
        while (!gameShapesQ.isEmpty()) {
            gameShapesQ.poll().render(shapeRenderer);
        }
        shapeRenderer.end();
    }

    @Override
    public void pause() {
        logger.log("Level screen pause method called");
        GameEngine engine = game.getGameEngine();
        sysStatesOnPause = engine.getCurrSysStates();
        engine.setAllSystemsOn(false);
        engine.setSystemsOn(true, SpriteSystem.class);
        AudioManager audioMan = game.getAudioMan();
        audioMan.pauseSound();
        audioMan.pauseMusic();
        audioMan.playSound(SoundAsset.PAUSE_SOUND);
    }

    @Override
    public void resume() {
        logger.log("Level screen resume method called");
        GameEngine engine = game.getGameEngine();
        logger.log("Sys states on resume: " + UtilMethods.toString(sysStatesOnPause));
        engine.setSysStates(sysStatesOnPause);
        AudioManager audioMan = game.getAudioMan();
        audioMan.resumeSound();
        if (!isPlayerDeathEvent()) {
            audioMan.resumeMusic();
        }
        audioMan.playSound(SoundAsset.PAUSE_SOUND);
    }

    @Override
    public void dispose() {
        set = false;
        spawnMan.reset();
        levelMapMan.dispose();
        playerSpawnMan.reset();
        game.getGameEngine().reset();
        game.getAudioMan().stopMusic();
        game.getEventMan().remove(this);
        for (Runnable r : runOnDispose) {
            r.run();
        }
    }

    private void spawnMegaman() {
        KeyValuePair<Vector2, ObjectMap<String, Object>> spawn = playerSpawnMan.getCurrPlayerCheckpoint();
        game.getGameEngine().spawnEntity(game.getMegaman(), spawn.key(), spawn.value());
        game.getEventMan().submitEvent(new Event(EventType.PLAYER_SPAWN));
    }

    private void addUiDrawable(Drawable drawable) {
        addUiDrawable(() -> true, drawable);
    }

    private void addUiDrawable(Supplier<Boolean> doDraw, Drawable drawable) {
        uiDrawables.add(KeyValuePair.of(doDraw, drawable));
    }

}
