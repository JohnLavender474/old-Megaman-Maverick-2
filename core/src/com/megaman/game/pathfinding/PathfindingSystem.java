package com.megaman.game.pathfinding;

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
    private final List<PathfindingComponent> pcs = new ArrayList<>();
    private final List<Pathfinder> pfs = new ArrayList<>();

    @Setter
    private WorldGraph worldGraph;

    public PathfindingSystem() {
        super(PathfindingComponent.class);
    }

    @Override
    protected void preProcess(float delta) {
        pcs.clear();
        pfs.clear();
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        PathfindingComponent pc = e.getComponent(PathfindingComponent.class);
        LinkedList<Vector2> path = pc.currPath;
        if (path != null) {
            while (!path.isEmpty() && pc.isPointReached(path.getFirst())) {
                path.poll();
            }
            if (path.peek() != null) {
                pc.consume(path.getFirst());
            }
        }
        pc.refreshTimer.update(delta);
        if (pc.refreshTimer.isFinished()) {
            pc.refreshTimer.reset();
            pcs.add(pc);
            pfs.add(new Pathfinder(worldGraph, pc));
        }
    }

    @Override
    protected void postProcess(float delta) {
        try {
            List<Future<LinkedList<Vector2>>> res = execServ.invokeAll(pfs);
            for (int i = 0; i < res.size(); i++) {
                PathfindingComponent pc = pcs.get(i);
                LinkedList<Vector2> pfRes = res.get(i).get();
                if (pfRes == null && pc.persistOldPath) {
                    continue;
                }
                pc.currPath = pfRes;
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public void dispose() {
        execServ.shutdown();
    }

}
