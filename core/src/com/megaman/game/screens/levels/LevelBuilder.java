package com.megaman.game.screens.levels;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.backgrounds.Background;
import com.megaman.game.entities.utils.factories.EntityFactories;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.impl.items.ItemFactory;
import com.megaman.game.entities.impl.megaman.upgrades.MegaHeartTank;
import com.megaman.game.entities.impl.sensors.SensorFactory;
import com.megaman.game.events.EventManager;
import com.megaman.game.events.EventType;
import com.megaman.game.screens.levels.map.LevelMapLayer;
import com.megaman.game.screens.levels.map.LevelMapObjParser;
import com.megaman.game.screens.levels.spawns.Spawn;
import com.megaman.game.screens.levels.spawns.SpawnType;
import com.megaman.game.screens.levels.spawns.impl.SpawnOnEventPredicate;
import com.megaman.game.screens.levels.spawns.impl.SpawnWhenInBounds;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Getter
public class LevelBuilder implements Disposable {

    private final Array<Spawn> spawns;
    private final Array<RectangleMapObject> playerSpawns;
    private final Array<RectangleMapObject> gameRoomObjs;
    private final Array<Background> backgrounds;
    private final Array<Runnable> runOnDispose;

    public LevelBuilder(MegamanGame game, Map<LevelMapLayer, Array<RectangleMapObject>> m) {
        backgrounds = new Array<>();
        runOnDispose = new Array<>();
        gameRoomObjs = m.get(LevelMapLayer.GAME_ROOMS);
        playerSpawns = m.get(LevelMapLayer.PLAYER_SPAWNS);
        Camera gameCam = game.getGameCam();
        GameEngine engine = game.getGameEngine();
        EventManager eventMan = game.getEventMan();
        EntityFactories factories = game.getEntityFactories();
        spawns = new Array<>();
        for (Map.Entry<LevelMapLayer, Array<RectangleMapObject>> e : m.entrySet()) {
            switch (e.getKey()) {
                case BACKGROUNDS -> {
                    for (RectangleMapObject o : e.getValue()) {
                        ObjectMap<String, Object> data = LevelMapObjParser.parse(o);
                        TextureRegion bkgReg = game.getAssMan().getTextureRegion(
                                TextureAsset.PREFIX + data.get(ConstKeys.ATLAS),
                                (String) data.get(ConstKeys.REGION));
                        backgrounds.add(new Background(bkgReg, o));
                    }
                }
                case GATES -> {
                    for (RectangleMapObject o : e.getValue()) {
                        engine.spawn(factories.fetch(EntityType.SENSOR, SensorFactory.GATE),
                                o.getRectangle(), LevelMapObjParser.parse(o));
                    }
                }
                case DEATH -> {
                    for (RectangleMapObject o : e.getValue()) {
                        engine.spawn(factories.fetch(EntityType.SENSOR, SensorFactory.DEATH), o.getRectangle());
                    }
                }
                case ENEMY_SPAWNS, BOSS_SPAWNS, BLOCKS, HAZARDS, SPECIAL, ITEMS -> {
                    EntityType entityType = switch (e.getKey()) {
                        case ENEMY_SPAWNS -> EntityType.ENEMY;
                        case BOSS_SPAWNS -> EntityType.BOSS;
                        case BLOCKS -> EntityType.BLOCK;
                        case HAZARDS -> EntityType.HAZARD;
                        case SPECIAL -> EntityType.SPECIAL;
                        case ITEMS -> EntityType.ITEM;
                        default -> throw new IllegalStateException("No matching entity type for: " + e.getKey());
                    };
                    for (RectangleMapObject o : e.getValue()) {
                        ObjectMap<String, Object> data = LevelMapObjParser.parse(o);
                        if (data.containsKey(SpawnType.SPAWN_TYPE)) {
                            switch ((String) data.get(SpawnType.SPAWN_TYPE)) {
                                case SpawnType.SPAWN_ROOM -> {
                                    String roomName = (String) data.get(SpawnType.SPAWN_ROOM);
                                    boolean roomFound = false;
                                    for (RectangleMapObject room : gameRoomObjs) {
                                        if (roomName.equals(room.getName())) {
                                            data.put(ConstKeys.SPAWN, o.getRectangle());
                                            data.put(ConstKeys.ROOM, room.getRectangle());
                                            spawns.add(new SpawnWhenInBounds(engine, gameCam, room.getRectangle(), data,
                                                    () -> factories.fetch(entityType, o.getName())));
                                            roomFound = true;
                                            break;
                                        }
                                    }
                                    if (!roomFound) {
                                        throw new IllegalStateException("Room not found: " + roomName);
                                    }
                                }
                                case SpawnType.SPAWN_EVENT -> {
                                    Set<EventType> eventTypes = EnumSet.noneOf(EventType.class);
                                    for (String spawnEvent : ((String) data.get(SpawnType.SPAWN_EVENT)).split(",")) {
                                        eventTypes.add(EventType.getEventType(spawnEvent));
                                    }
                                    SpawnOnEventPredicate s = new SpawnOnEventPredicate(engine, o.getRectangle(), data,
                                            () -> factories.fetch(entityType, o.getName()),
                                            event -> eventTypes.contains(event.type));
                                    spawns.add(s);
                                    eventMan.add(s);
                                    runOnDispose.add(() -> eventMan.remove(s));
                                }
                            }
                        } else if (o.getName() != null) {
                            switch (o.getName()) {
                                case ItemFactory.HEART_TANK -> {
                                    if (game.getMegaman().has(MegaHeartTank.get((String) data.get(ConstKeys.VAL)))) {
                                        continue;
                                    }
                                    spawns.add(new SpawnWhenInBounds(engine, gameCam, o.getRectangle(), data,
                                            () -> factories.fetch(entityType, o.getName()), false));
                                }
                                default -> spawns.add(new SpawnWhenInBounds(engine, gameCam, o.getRectangle(), data,
                                        () -> factories.fetch(entityType, o.getName())));
                            }
                        } else {
                            spawns.add(new SpawnWhenInBounds(engine, gameCam, o.getRectangle(), data,
                                    () -> factories.fetch(entityType, o.getName())));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        runOnDispose.forEach(Runnable::run);
    }

}
