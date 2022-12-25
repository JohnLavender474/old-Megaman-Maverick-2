package com.megaman.game.screens.levels;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.megaman.game.audio.SoundSystem;
import com.megaman.game.backgrounds.Background;
import com.megaman.game.behaviors.BehaviorSystem;
import com.megaman.game.controllers.ControllerBtn;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.controllers.ControllerSystem;
import com.megaman.game.cull.CullOnOutOfBoundsSystem;
import com.megaman.game.entities.EntityFactories;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.megaman.weapons.MegamanWeapon;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.events.EventManager;
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
import com.megaman.game.screens.ui.BitsBar;
import com.megaman.game.screens.ui.TextHandle;
import com.megaman.game.shapes.LineSystem;
import com.megaman.game.shapes.RenderableShape;
import com.megaman.game.shapes.ShapeSystem;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.sprites.SpriteSystem;
import com.megaman.game.updatables.UpdatableSystem;
import com.megaman.game.utils.ConstFuncs;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.objs.KeyValuePair;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldGraph;
import com.megaman.game.world.WorldSystem;
import com.megaman.game.world.WorldVals;

import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Supplier;

public class LevelScreen extends ScreenAdapter implements EventListener {

    private static final Logger logger = new Logger(LevelScreen.class, MegamanGame.DEBUG && true);

    private static final float ON_PLAYER_DEATH_DELAY = 4f;

    private final MegamanGame game;
    private final LevelMapManager levelMapMan;
    private final LevelCamManager levelCamMan;
    private final SpawnManager spawnMan;
    private final PlayerSpawnManager playerSpawnMan;
    private final OrthographicCamera uiCam;
    private final OrthographicCamera gameCam;
    private final Array<Runnable> runOnDispose = new Array<>();
    private final Timer playerDeathDelayTimer = new Timer(ON_PLAYER_DEATH_DELAY, true);
    private final Array<Background> backgrounds = new Array<>();
    private final PriorityQueue<SpriteHandle> gameSpritesQ = new PriorityQueue<>();
    private final PriorityQueue<RenderableShape> gameShapesQ = new PriorityQueue<>();
    private final Queue<TextHandle> uiText = new LinkedList<>();
    private final Array<KeyValuePair<Supplier<Boolean>, Drawable>> uiDrawables = new Array<>();

    private OrderedMap<Class<? extends System>, Boolean> sysStatesOnPause;
    private boolean set;
    private Music music;

    public LevelScreen(MegamanGame game) {
        this.game = game;
        uiCam = game.getUiCam();
        gameCam = game.getGameCam();
        spawnMan = new SpawnManager();
        playerSpawnMan = new PlayerSpawnManager();
        levelCamMan = new LevelCamManager(gameCam);
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

    public void set(Level level) {
        dispose();
        if (level.getMusicAsset() != null) {
            setMusic(level.getMusicAsset());
            playMusic(true);
        }
        playerDeathDelayTimer.setToEnd();
        // reset cam positions
        uiCam.position.set(ConstFuncs.getCamInitPos());
        gameCam.position.set(ConstFuncs.getCamInitPos());
        // set systems
        GameEngine engine = game.getGameEngine();
        engine.setAllSystemsOn(true);
        engine.getSystem(SpriteSystem.class).set(gameCam, gameSpritesQ);
        engine.getSystem(LineSystem.class).setGameShapesQ(gameShapesQ);
        engine.getSystem(ShapeSystem.class).setGameShapesQ(gameShapesQ);
        engine.getSystem(CullOnOutOfBoundsSystem.class).setGameCam(gameCam);
        // set level map, get layer objs
        Map<LevelMapLayer, Array<RectangleMapObject>> m = levelMapMan.set(level.getTmxFile());
        // set world system
        WorldGraph worldGraph = new WorldGraph(levelMapMan.getWorldWidth(), levelMapMan.getWorldHeight());
        engine.getSystem(WorldSystem.class).setWorldGraph(worldGraph);
        engine.getSystem(PathfindingSystem.class).setWorldGraph(worldGraph);
        // set game rooms
        Array<RectangleMapObject> gameRoomsObjs = m.get(LevelMapLayer.GAME_ROOMS);
        levelCamMan.set(gameRoomsObjs, game.getMegaman());
        // set player spawns
        Array<RectangleMapObject> playerSpawns = m.get(LevelMapLayer.PLAYER_SPAWNS);
        playerSpawnMan.set(gameCam, playerSpawns);
        // set spawns
        Array<Spawn> spawns = new Array<>();
        EntityFactories factories = game.getEntityFactories();
        for (Map.Entry<LevelMapLayer, Array<RectangleMapObject>> e : m.entrySet()) {
            switch (e.getKey()) {
                case ENEMY_SPAWNS, BLOCKS, HAZARDS, SENSORS, SPECIAL -> {
                    EntityType type = switch (e.getKey()) {
                        case ENEMY_SPAWNS -> EntityType.ENEMY;
                        case BLOCKS -> EntityType.BLOCK;
                        case HAZARDS -> EntityType.HAZARD;
                        case SENSORS -> EntityType.SENSOR;
                        case SPECIAL -> EntityType.SPECIAL;
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
                                        throw new IllegalStateException("Failed to create spawn for room: " + roomName);
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

    public void setMusic(MusicAsset m) {
        if (music != null) {
            music.stop();
        }
        music = game.getAssMan().getMusic(m);
    }

    public void playMusic(boolean loop) {
        if (music != null && music.isPlaying()) {
            music.stop();
        }
        game.getAudioMan().playMusic(music, loop);
    }

    public void stopMusic() {
        if (music != null) {
            music.stop();
        }
    }

    public void pauseMusic() {
        if (music != null) {
            music.pause();
        }
    }

    @Override
    public void listenForEvent(Event event) {
        GameEngine engine = game.getGameEngine();
        switch (event.eventType) {
            case GAME_PAUSE -> pause();
            case GAME_RESUME -> resume();
            case PLAYER_DEAD -> {
                playerDeathDelayTimer.reset();
                game.getAudioMan().playSound(SoundAsset.MEGAMAN_DEFEAT_SOUND);
                stopMusic();
            }
            case GATE_INIT_OPENING -> {
                engine.setSystemsOn(false,
                        ControllerSystem.class,
                        TrajectorySystem.class,
                        BehaviorSystem.class,
                        WorldSystem.class);
            }
            case NEXT_GAME_ROOM_REQ -> {
                String n = event.getInfo(ConstKeys.ROOM, RectangleMapObject.class).getName();
                levelCamMan.transToRoom(n);
            }
            case ENTER_BOSS_ROOM -> {
            }
        }
    }

    @Override
    public void show() {
        spawnMegaman();
        game.getEventMan().add(this);
    }

    @Override
    public void render(float delta) {
        if (!set) {
            throw new IllegalStateException("Must call set method before rendering");
        }
        super.render(delta);
        ControllerManager ctrlMan = game.getCtrlMan();
        if (ctrlMan.isJustPressed(ControllerBtn.START)) {
            if (game.isPaused()) {
                game.resume();
            } else {
                game.pause();
            }
        }
        Megaman megaman = game.getMegaman();
        GameEngine engine = game.getGameEngine();
        EventManager eventMan = game.getEventMan();
        if (!game.isPaused()) {
            levelCamMan.update(delta);
            if (levelCamMan.getTransState() == null) {
                playerSpawnMan.run();
                spawnMan.update(delta);
            } else {
                switch (levelCamMan.getTransState()) {
                    case BEGIN -> {
                        engine.setSystemsOn(false,
                                ControllerSystem.class,
                                TrajectorySystem.class,
                                UpdatableSystem.class,
                                BehaviorSystem.class,
                                WorldSystem.class,
                                SoundSystem.class);
                        eventMan.dispatchEvent(new Event(EventType.BEGIN_GAME_ROOM_TRANS, new ObjectMap<>() {{
                            put(ConstKeys.POS, levelCamMan.getTransInterpolation());
                        }}));
                        ShapeUtils.setBottomCenterToPoint(megaman.body.bounds, levelCamMan.getTransInterpolation());
                    }
                    case CONTINUE -> {
                        eventMan.dispatchEvent(new Event(EventType.CONTINUE_GAME_ROOM_TRANS, new ObjectMap<>() {{
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
                        eventMan.dispatchEvent(new Event(EventType.END_GAME_ROOM_TRANS, new ObjectMap<>() {{
                            put(ConstKeys.ROOM, levelCamMan.getCurrGameRoom());
                        }}));
                    }
                }
            }
            playerDeathDelayTimer.update(delta);
            if (playerDeathDelayTimer.isJustFinished()) {
                spawnMegaman();
            }
        }
        engine.update(delta);
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(gameCam.combined);
        batch.begin();
        for (Background b : backgrounds) {
            b.update(delta);
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
            uiDrawable.value().draw(batch);
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
        game.getAudioMan().playSound(SoundAsset.PAUSE_SOUND);
    }

    @Override
    public void resume() {
        logger.log("Level screen resume method called");
        GameEngine engine = game.getGameEngine();
        logger.log("Sys states on resume: " + UtilMethods.toString(sysStatesOnPause));
        engine.setSysStates(sysStatesOnPause);
        game.getAudioMan().playSound(SoundAsset.PAUSE_SOUND);
    }

    @Override
    public void dispose() {
        set = false;
        stopMusic();
        spawnMan.reset();
        levelMapMan.dispose();
        playerSpawnMan.reset();
        game.getGameEngine().reset();
        game.getEventMan().remove(this);
        for (Runnable r : runOnDispose) {
            r.run();
        }
    }

    private void spawnMegaman() {
        KeyValuePair<Vector2, ObjectMap<String, Object>> spawn = playerSpawnMan.getCurrPlayerCheckpoint();
        game.getGameEngine().spawnEntity(game.getMegaman(), spawn.key(), spawn.value());
        game.getEventMan().dispatchEvent(new Event(EventType.PLAYER_SPAWN, new ObjectMap<>() {{
            put(ConstKeys.ROOM, levelCamMan.getCurrGameRoom());
        }}));
    }

    private void addUiDrawable(Drawable drawable) {
        addUiDrawable(() -> true, drawable);
    }

    private void addUiDrawable(Supplier<Boolean> doDraw, Drawable drawable) {
        uiDrawables.add(KeyValuePair.of(doDraw, drawable));
    }

}
