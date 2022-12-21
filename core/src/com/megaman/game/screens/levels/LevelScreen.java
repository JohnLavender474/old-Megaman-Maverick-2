package com.megaman.game.screens.levels;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.megaman.game.ConstKeys;
import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.ViewVals;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.MusicAsset;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.AudioManager;
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
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.events.EventManager;
import com.megaman.game.events.EventType;
import com.megaman.game.movement.trajectory.TrajectorySystem;
import com.megaman.game.screens.levels.camera.LevelCamManager;
import com.megaman.game.screens.levels.map.LevelMapLayer;
import com.megaman.game.screens.levels.map.LevelMapManager;
import com.megaman.game.screens.levels.map.LevelMapObjParser;
import com.megaman.game.screens.levels.spawns.LevelSpawn;
import com.megaman.game.screens.levels.spawns.LevelSpawnManager;
import com.megaman.game.shapes.LineSystem;
import com.megaman.game.shapes.RenderableShape;
import com.megaman.game.shapes.ShapeSystem;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.sprites.SpriteSystem;
import com.megaman.game.ui.BitsBar;
import com.megaman.game.ui.TextHandle;
import com.megaman.game.updatables.UpdatableSystem;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.objs.KeyValuePair;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldSystem;
import com.megaman.game.world.WorldVals;

import java.util.*;
import java.util.function.Supplier;

public class LevelScreen extends ScreenAdapter implements EventListener {

    private static final float ON_PLAYER_DEATH_DELAY = 4f;

    private final MegamanGame game;
    private final LevelSpawnManager spawnMan;
    private final LevelMapManager levelMapMan;
    private final LevelCamManager levelCamMan;

    private final OrthographicCamera gameCam;
    private final OrthographicCamera uiCam;
    private final Viewport gameViewport;
    private final Viewport uiViewport;

    private final Sound playerDeathSound;
    private final Timer playerDeathDelayTimer = new Timer(ON_PLAYER_DEATH_DELAY, true);

    private final Array<Background> backgrounds = new Array<>();
    private final PriorityQueue<SpriteHandle> gameSpritesQ = new PriorityQueue<>();
    private final Map<ShapeRenderer.ShapeType, Queue<RenderableShape>> shapeRenderQs =
            new EnumMap<>(ShapeRenderer.ShapeType.class) {{
                for (ShapeRenderer.ShapeType s : ShapeRenderer.ShapeType.values()) {
                    put(s, new LinkedList<>());
                }
            }};

    private final Queue<TextHandle> uiText = new LinkedList<>();
    private final Array<KeyValuePair<Supplier<Boolean>, Drawable>> uiDrawables = new Array<>();

    private boolean paused;
    private boolean set;
    private Music music;

    public LevelScreen(MegamanGame game) {
        this.game = game;
        // cameras and viewports
        float screenWidth = ViewVals.VIEW_WIDTH * WorldVals.PPM;
        float screenHeight = ViewVals.VIEW_HEIGHT * WorldVals.PPM;
        gameCam = new OrthographicCamera();
        uiCam = new OrthographicCamera();
        gameViewport = new FitViewport(screenWidth, screenHeight, gameCam);
        uiViewport = new FitViewport(screenWidth, screenHeight, uiCam);
        // level managers
        spawnMan = new LevelSpawnManager();
        levelCamMan = new LevelCamManager(gameCam);
        levelMapMan = new LevelMapManager(gameCam, game.getBatch());
        // drawables
        AssetsManager assMan = game.getAssMan();
        BitsBar healthBar = new BitsBar(
                () -> game.getMegaman().getHealth(),
                assMan.getTextureRegion(TextureAsset.BITS, "StandardBit"),
                assMan.getTextureRegion(TextureAsset.DECORATIONS, "Black"));
        addUiDrawable(healthBar);
        // sounds
        playerDeathSound = assMan.getSound(SoundAsset.MEGAMAN_DEFEAT_SOUND);
    }

    public void set(String tmxFile) {
        GameEngine engine = game.getGameEngine();
        engine.getSystem(SpriteSystem.class).set(gameCam, gameSpritesQ);
        engine.getSystem(LineSystem.class).setShapeRenderQs(shapeRenderQs);
        engine.getSystem(ShapeSystem.class).setShapeRenderQs(shapeRenderQs);
        engine.getSystem(CullOnOutOfBoundsSystem.class).setGameCam(gameCam);
        Map<LevelMapLayer, Array<RectangleMapObject>> m = levelMapMan.set(tmxFile);
        engine.getSystem(WorldSystem.class).setWorldGraph(levelMapMan.getWorldWidth(), levelMapMan.getWorldHeight());
        Array<RectangleMapObject> playerSpawns = new Array<>();
        Array<LevelSpawn> spawns = new Array<>();
        EntityFactories factories = game.getEntityFactories();
        for (Map.Entry<LevelMapLayer, Array<RectangleMapObject>> e : m.entrySet()) {
            switch (e.getKey()) {
                case GAME_ROOMS -> levelCamMan.set(e.getValue(), game.getMegaman());
                case PLAYER_SPAWNS -> playerSpawns.addAll(e.getValue());
                case ENEMY_SPAWNS, BLOCKS, HAZARDS, SENSORS, SPECIAL -> {
                    EntityType type = switch (e.getKey()) {
                        case ENEMY_SPAWNS -> EntityType.ENEMY;
                        case BLOCKS -> EntityType.BLOCK;
                        case HAZARDS -> EntityType.HAZARD;
                        case SENSORS -> EntityType.SENSOR;
                        case SPECIAL -> EntityType.SPECIAL;
                        default -> throw new IllegalStateException("Incompatible state");
                    };
                    for (RectangleMapObject o : e.getValue()) {
                        spawns.add(new LevelSpawn(
                                o.getRectangle(),
                                () -> factories.fetch(type, o.getName()),
                                () -> LevelMapObjParser.parse(o)));
                    }
                }
            }
        }
        spawnMan.set(playerSpawns, spawns);
        playerDeathDelayTimer.setToEnd();
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

    @Override
    public void listenForEvent(Event event) {
        GameEngine engine = game.getGameEngine();
        switch (event.eventType) {
            case GAME_PAUSE -> pause();
            case GAME_RESUME -> resume();
            case PLAYER_DEAD -> {
                playerDeathDelayTimer.reset();
                engine.getSystem(SoundSystem.class).reqStopAllLoops();
                game.getAudioMan().playSound(playerDeathSound, false);
                stopMusic();
            }
            case GATE_INIT_OPENING -> {
                engine.setSystemsOn(false,
                        ControllerSystem.class,
                        TrajectorySystem.class,
                        BehaviorSystem.class,
                        WorldSystem.class);
                engine.getSystem(SoundSystem.class).reqStopAllLoops();
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
        paused = false;
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
            if (paused) {
                resume();
            } else {
                pause();
            }
        }
        GameEngine engine = game.getGameEngine();
        EventManager eventMan = game.getEventMan();
        Megaman megaman = game.getMegaman();
        if (!paused) {
            levelCamMan.update(delta);
            if (levelCamMan.getTransState() == null) {
                spawnMan.update(engine, gameCam);
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
                        engine.getSystem(SoundSystem.class).reqStopAllLoops();
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
        for (Map.Entry<ShapeRenderer.ShapeType, Queue<RenderableShape>> e : shapeRenderQs.entrySet()) {
            shapeRenderer.begin(e.getKey());
            Queue<RenderableShape> q = e.getValue();
            while (!q.isEmpty()) {
                q.poll().render(shapeRenderer);
            }
            shapeRenderer.end();
        }
        gameViewport.apply();
        uiViewport.apply();
    }

    @Override
    public void pause() {
        if (paused) {
            return;
        }
        paused = true;
        AudioManager audioMan = game.getAudioMan();
        audioMan.scaleSoundVolume(.5f);
        audioMan.scaleMusicVolume(.5f);
        Sound pauseSound = game.getAssMan().getSound(SoundAsset.PAUSE_SOUND);
        audioMan.playSound(pauseSound, false);
    }

    @Override
    public void resume() {
        if (!paused) {
            return;
        }
        paused = false;
        AudioManager audioMan = game.getAudioMan();
        audioMan.scaleSoundVolume(2f);
        audioMan.scaleMusicVolume(2f);
        Sound pauseSound = game.getAssMan().getSound(SoundAsset.PAUSE_SOUND);
        audioMan.playSound(pauseSound, false);
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        uiViewport.update(width, height);
    }

    @Override
    public void dispose() {
        set = false;
        stopMusic();
        spawnMan.reset();
        levelMapMan.dispose();
        game.getGameEngine().reset();
        game.getEventMan().remove(this);
    }

    private void spawnMegaman() {
        KeyValuePair<Vector2, ObjectMap<String, Object>> spawn = spawnMan.getCurrPlayerCheckpoint();
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
