package com.megaman.game.screens.levels.spawns.player;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.screens.levels.map.LevelMapObjParser;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.interfaces.Resettable;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class PlayerSpawnManager implements Runnable, Resettable {

    private final Camera gameCam;

    private RectangleMapObject curr;
    private Queue<RectangleMapObject> spawns;

    public PlayerSpawnManager(Camera gameCam) {
        this.gameCam = gameCam;
    }

    public void set(Iterable<RectangleMapObject> playerSpawns) {
        this.spawns = new PriorityQueue<>(Comparator.comparing(p -> Integer.valueOf(p.getName())));
        for (RectangleMapObject playerSpawn : playerSpawns) {
            this.spawns.add(playerSpawn);
        }
        this.curr = this.spawns.poll();
    }

    public Vector2 getSpawn() {
        return ShapeUtils.getBottomCenterPoint(curr.getRectangle());
    }

    public ObjectMap<String, Object> getData() {
        return LevelMapObjParser.parse(curr);
    }

    @Override
    public void run() {
        if (gameCam == null || spawns == null) {
            throw new IllegalStateException("Must call set method before running");
        }
        if (!spawns.isEmpty() && UtilMethods.isInCamBounds(gameCam, spawns.peek().getRectangle())) {
            curr = spawns.poll();
        }
    }

    @Override
    public void reset() {
        spawns = null;
        curr = null;
    }

}
