package com.megaman.game.screens.levels.spawns.player;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.screens.levels.map.LevelMapObjParser;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.objs.KeyValuePair;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class PlayerSpawnManager implements Runnable, Resettable {

    private Camera gameCam;
    private Queue<RectangleMapObject> playerSpawns;
    private RectangleMapObject currPlayerSpawn;

    public void set(Camera gameCam, Iterable<RectangleMapObject> playerSpawns) {
        this.gameCam = gameCam;
        this.playerSpawns = new PriorityQueue<>(Comparator.comparing(p -> Integer.valueOf(p.getName())));
        for (RectangleMapObject playerSpawn : playerSpawns) {
            this.playerSpawns.add(playerSpawn);
        }
        this.currPlayerSpawn = this.playerSpawns.poll();
    }

    public KeyValuePair<Vector2, ObjectMap<String, Object>> getCurrPlayerCheckpoint() {
        if (currPlayerSpawn == null) {
            throw new IllegalStateException("No player spawn present");
        }
        return KeyValuePair.of(
                ShapeUtils.getBottomCenterPoint(currPlayerSpawn.getRectangle()),
                LevelMapObjParser.parse(currPlayerSpawn));
    }

    @Override
    public void run() {
        if (gameCam == null || playerSpawns == null) {
            throw new IllegalStateException("Must call set method before running");
        }
        if (!playerSpawns.isEmpty() && UtilMethods.isInCamBounds(gameCam, playerSpawns.peek().getRectangle())) {
            currPlayerSpawn = playerSpawns.poll();
        }
    }

    @Override
    public void reset() {
        gameCam = null;
        playerSpawns = null;
        currPlayerSpawn = null;
    }

}
