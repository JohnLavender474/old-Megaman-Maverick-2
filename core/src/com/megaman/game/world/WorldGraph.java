package com.megaman.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.megaman.game.graph.Graph;
import com.megaman.game.utils.interfaces.Resettable;

public class WorldGraph extends Graph<WorldNode> implements Resettable {

    public WorldGraph(int width, int height) {
        super(createNodes(width, height));
    }

    public static WorldNode[][] createNodes(int width, int height) {
        WorldNode[][] worldNodes = new WorldNode[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                worldNodes[x][y] = new WorldNode(x, y);
            }
        }
        return worldNodes;
    }

    public WorldNode getNode(Vector2 pos) {
        int x = Math.round(pos.x / WorldConstVals.PPM);
        int y = Math.round(pos.y / WorldConstVals.PPM);
        return getNode(x, y);
    }

    public Array<WorldNode> getNodesOverlapping(Rectangle bounds) {
        int[] m = getMinsAndMaxes(bounds);
        int xMin = m[0];
        int yMin = m[1];
        int xMax = m[2];
        int yMax = m[3];
        Array<WorldNode> nodeList = new Array<>((xMax - xMin) * (yMax - yMin));
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                nodeList.add(nodes[x][y]);
            }
        }
        return nodeList;
    }

    private int[] getMinsAndMaxes(Rectangle bounds) {
        int minX = (int) Math.floor(bounds.x / WorldConstVals.PPM);
        int minY = (int) Math.floor(bounds.y / WorldConstVals.PPM);
        int maxX = (int) Math.ceil((bounds.x + bounds.width) / WorldConstVals.PPM);
        int maxY = (int) Math.ceil((bounds.y + bounds.height) / WorldConstVals.PPM);
        return new int[] {
                Integer.max(0, minX),
                Integer.max(0, minY),
                Integer.min(nodes.length, maxX),
                Integer.min(nodes[0].length, maxY)
        };
    }

    public void addBody(Body body) {
        Array<WorldNode> worldNodes = getNodesOverlapping(body.bounds);
        for (WorldNode n : worldNodes) {
            n.bodies.add(body);
        }
    }

    public void addFixture(Fixture fixture) {
        Array<WorldNode> worldNodes = getNodesOverlapping(fixture.bounds);
        for (WorldNode n : worldNodes) {
            n.fixtures.add(fixture);
        }
    }

    public Array<Body> getBodiesOverlapping(Body body) {
        Array<WorldNode> worldNodes = getNodesOverlapping(body.bounds);
        Array<Body> overlappingBodies = new Array<>(30);
        ObjectSet<Body> alreadyChecked = new ObjectSet<>(30);
        for (WorldNode n : worldNodes) {
            for (Body o : n.bodies) {
                if (alreadyChecked.contains(o)) {
                    continue;
                }
                alreadyChecked.add(o);
                if (body.overlaps(o)) {
                    overlappingBodies.add(o);
                }
            }
        }
        return overlappingBodies;
    }

    public Array<Fixture> getFixturesOverlapping(Fixture fixture) {
        Array<WorldNode> worldNodes = getNodesOverlapping(fixture.bounds);
        Array<Fixture> overlappingFixtures = new Array<>(30);
        ObjectSet<Fixture> alreadyChecked = new ObjectSet<>(30);
        for (WorldNode n : worldNodes) {
            for (Fixture o : n.fixtures) {
                if (alreadyChecked.contains(o)) {
                    continue;
                }
                alreadyChecked.add(o);
                if (fixture.overlaps(o)) {
                    overlappingFixtures.add(o);
                }
            }
        }
        return overlappingFixtures;
    }

    @Override
    public void reset() {
        forEachNode(WorldNode::reset);
    }

}
