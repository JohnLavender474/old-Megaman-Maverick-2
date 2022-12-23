package com.megaman.game.pathfinding;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.world.WorldGraph;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PathfindingSystem extends System implements Disposable {

    private final ExecutorService execServ = Executors.newCachedThreadPool();
    private final List<PathfindingComponent> pCompList = new ArrayList<>();
    private final List<Pathfinder> pfinderList = new ArrayList<>();

    @Setter
    private WorldGraph worldGraph;

    public PathfindingSystem() {
        super(PathfindingComponent.class);
    }

    @Override
    protected void preProcess(float delta) {
        pCompList.clear();
        pfinderList.clear();
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        PathfindingComponent c = e.getComponent(PathfindingComponent.class);
        PathfindParams params = c.params;
        LinkedList<Rectangle> path = c.currentPath;
        if (path == null) {
            c.currentTrajectory = Vector2.Zero;
        } else {
            while (!path.isEmpty() && params.isTargetReached(path.getFirst())) {
                path.poll();
            }
            if (path.peek() != null) {
                c.currentTrajectory = params.apply(path.getFirst());
            }
        }
        params.refreshTimer.update(delta);
        if (params.refreshTimer.isFinished()) {
            params.refreshTimer.reset();
            pCompList.add(c);
            pfinderList.add(new Pathfinder(worldGraph, params));
        }
    }

    @Override
    protected void postProcess(float delta) {
        try {
            List<Future<LinkedList<Rectangle>>> res = execServ.invokeAll(pfinderList);
            for (int i = 0; i < res.size(); i++) {
                PathfindingComponent c = pCompList.get(i);
                c.currentPath = res.get(i).get();
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public void dispose() {
        execServ.shutdown();
    }

}
