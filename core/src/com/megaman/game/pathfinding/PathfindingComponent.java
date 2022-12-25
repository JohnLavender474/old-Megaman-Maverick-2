package com.megaman.game.pathfinding;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.Component;

import java.util.LinkedList;

public class PathfindingComponent implements Component {

    public PathfindParams params;
    public Vector2 currentTrajectory;
    public LinkedList<Rectangle> currentPath;

    public PathfindingComponent(PathfindParams params) {
        this.params = params;
        reset();
    }

    @Override
    public void reset() {
        currentPath = new LinkedList<>();
        currentTrajectory = new Vector2();
    }

}
