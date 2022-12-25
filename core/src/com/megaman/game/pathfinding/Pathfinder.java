package com.megaman.game.pathfinding;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.MegamanGame;
import com.megaman.game.utils.Logger;
import com.megaman.game.world.WorldGraph;
import com.megaman.game.world.WorldVals;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

public class Pathfinder implements Callable<LinkedList<Rectangle>> {

    private static final Logger logger = new Logger(Pathfinder.class, MegamanGame.DEBUG && false);

    private Node[][] nodes;
    private WorldGraph graph;
    private PathfindParams params;

    public Pathfinder(WorldGraph graph, PathfindParams params) {
        this.graph = graph;
        this.params = params;
        this.nodes = new Node[graph.width][graph.height];
    }

    @Override
    public LinkedList<Rectangle> call() {
        int[] targetCoords = toGraphCoords(params.getTarget());
        int[] startCoords = toGraphCoords(params.getStart());
        Node startNode = nodes[startCoords[0]][startCoords[1]] = new Node(startCoords[0], startCoords[1]);
        PriorityQueue<Node> open = new PriorityQueue<>();
        open.add(startNode);
        while (!open.isEmpty()) {
            Node curr = open.poll();
            curr.disc = true;
            if (curr.x == targetCoords[0] && curr.y == targetCoords[1]) {
                LinkedList<Rectangle> path = new LinkedList<>();
                while (curr != null) {
                    path.addFirst(curr.worldBounds);
                    curr = curr.prev;
                }
                logger.log("Path: " + path);
                return path;
            }
            for (int x = curr.x - 1; x <= curr.x + 1; x++) {
                for (int y = curr.y - 1; y <= curr.y + 1; y++) {
                    if (isOutOfBounds(x, y)) {
                        continue;
                    }
                    if (!params.allowDiagonal) {
                        if (x == curr.x - 1 || x == curr.x + 1) {
                            if (y == curr.y - 1 || y == curr.y + 1) {
                                continue;
                            }
                        }
                    }
                    Node neighbor = nodes[x][y];
                    if (neighbor != null && neighbor.disc) {
                        continue;
                    }
                    if (x != targetCoords[0] &&
                            y != targetCoords[1] &&
                            params.reject(graph.getFixtures(x, y))) {
                        continue;
                    }
                    // int totalDist = curr.dist + dist(curr.x, curr.y, x, y);
                    int totalDist = curr.dist + cost(curr.x, curr.y, x, y);
                    if (neighbor == null) {
                        neighbor = nodes[x][y] = new Node(x, y);
                        neighbor.dist = totalDist;
                        neighbor.prev = curr;
                        open.add(neighbor);
                    } else if (totalDist < neighbor.dist) {
                        neighbor.dist = totalDist;
                        neighbor.prev = curr;
                        open.remove(neighbor);
                        open.add(neighbor);
                    }
                }
            }
        }
        logger.log("Return null");
        return null;
    }

    private int[] toGraphCoords(Vector2 pos) {
        int x = (int) (pos.x / WorldVals.PPM);
        int y = (int) (pos.y / WorldVals.PPM);
        if (x < 0) {
            x = 0;
        } else if (x >= nodes.length) {
            x = nodes.length - 1;
        }
        if (y < 0) {
            y = 0;
        } else if (y >= nodes[0].length) {
            y = nodes[0].length - 1;
        }
        return new int[]{x, y};
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= nodes.length || y < 0 || y >= nodes[0].length;
    }

    public int cost(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static final class Node implements Comparable<Node> {

        private final int x;
        private final int y;
        private final Rectangle worldBounds;

        private int dist;
        private Node prev;
        private boolean disc;

        private Node(int x, int y) {
            this.x = x;
            this.y = y;
            worldBounds = new Rectangle()
                    .setPosition(x * WorldVals.PPM, y * WorldVals.PPM)
                    .setSize(WorldVals.PPM);
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(dist, o.dist);
        }

        @Override
        public int hashCode() {
            int hash = 49;
            hash += 7 * x;
            hash += 7 * y;
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Node n && x == n.x && y == n.y;
        }

        @Override
        public String toString() {
            return "[" + x + ", " + y + "]: " + worldBounds;
        }

    }

}
