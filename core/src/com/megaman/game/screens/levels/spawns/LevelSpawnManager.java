package com.megaman.game.screens.levels.spawns;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.GameEngine;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.screens.levels.map.LevelMapObjParser;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.objs.KeyValuePair;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class LevelSpawnManager {

    private Array<LevelSpawn<Enemy>> enemySpawns;
    private RectangleMapObject currPlayerCheckpoint;
    private Queue<RectangleMapObject> playerCheckpoints;

    public void set(Array<LevelSpawn<Enemy>> enemySpawns, Array<RectangleMapObject> playerCheckpointObjs) {
        this.enemySpawns = enemySpawns;
        playerCheckpoints = new LinkedList<>();
        playerCheckpointObjs.sort(Comparator.comparing(p -> Integer.valueOf(p.getName())));
        for (RectangleMapObject p : playerCheckpointObjs) {
            playerCheckpoints.add(p);
        }
        currPlayerCheckpoint = playerCheckpoints.poll();
    }

    public KeyValuePair<Vector2, ObjectMap<String, Object>> getCurrPlayerCheckpoint() {
        if (currPlayerCheckpoint == null) {
            throw new IllegalStateException("No player init present");
        }
        return KeyValuePair.of(
                ShapeUtils.getBottomCenterPoint(currPlayerCheckpoint.getRectangle()),
                LevelMapObjParser.parse(currPlayerCheckpoint));
    }

    public void update(GameEngine engine, Camera cam) {
        if (enemySpawns == null || playerCheckpoints == null) {
            throw new IllegalStateException("Set method must first be called with non-null values");
        }
        for (LevelSpawn<Enemy> s : enemySpawns) {
            s.update(engine, cam);
        }
        if (playerCheckpoints.isEmpty()) {
            return;
        }
        RectangleMapObject nextPSpawn = playerCheckpoints.peek();
        if (UtilMethods.isInCamBounds(cam, nextPSpawn.getRectangle())) {
            currPlayerCheckpoint = playerCheckpoints.poll();
        }
    }

}
