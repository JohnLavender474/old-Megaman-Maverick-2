package com.megaman.game.pathfinding;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.megaman.game.world.WorldVals;
import com.megaman.game.world.WorldGraph;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class Pathfinder implements Callable<LinkedList<Vector2>> {

    private final WorldGraph graph;
    private final PathfindingComponent pc;

    @Override
    public LinkedList<Vector2> call() {
        /*
        LinkedList<Vector2> path = new LinkedList<>();
        // get start node
        WorldNode sNode = graph.getNode(pc.getStart());
        if (sNode == null) {
            return path;
        }
        // get target node
        WorldNode tNode = graph.getNode(pc.getTarget());
        if (tNode == null) {
            return path;
        }
        // node handles, open and closed
        NodeHandle[][] nodeHandles = new NodeHandle[graph.width][graph.height];
        ObjectSet<WorldNode> closed = new ObjectSet<>();
        PriorityQueue<NodeHandle> open = new PriorityQueue<>();
        open.add(new NodeHandle(sNode, 0, null));
        // while there are open nodes
        while (!open.isEmpty()) {
            NodeHandle currNHandle = open.poll();
            closed.add(currNHandle.node);
            if (currNHandle.node.equals(tNode)) {
                while (currNHandle != null) {
                    path.addFirst(currNHandle.node.getPosition().cpy().scl(WorldVals.PPM));
                    currNHandle = currNHandle.pred;
                }
                break;
            }
            Array<WorldNode> neighbors = graph.getNeighbors(currNHandle.node, pc.allowDiagonal);
            for (WorldNode neighbor : neighbors) {
                if (closed.contains(neighbor) || pc.isReject(neighbor)) {
                    continue;
                }
                int totalDist = currNHandle.dist + (int) currNHandle.node.getPosition().dst(neighbor.getPosition());
                NodeHandle neighborNHandle = nodeHandles[neighbor.x][neighbor.y];
                if (neighborNHandle == null) {
                    neighborNHandle = new NodeHandle(neighbor, totalDist, currNHandle);
                    nodeHandles[neighbor.x][neighbor.y] = neighborNHandle;
                    open.add(neighborNHandle);
                } else if (totalDist < neighborNHandle.dist) {
                    neighborNHandle.dist = totalDist;
                    neighborNHandle.pred = currNHandle;
                    open.remove(neighborNHandle);
                    open.add(neighborNHandle);
                }
            }
        }
        return path;
         */
        return new LinkedList<>();
    }

    /*
    @AllArgsConstructor
    private static final class NodeHandle implements Comparable<NodeHandle> {

        private WorldNode node;
        private Integer dist;
        private NodeHandle pred;

        @Override
        public int compareTo(NodeHandle o) {
            return dist.compareTo(o.dist);
        }

        @Override
        public int hashCode() {
            return node.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof NodeHandle nHandle && node.equals(nHandle.node);
        }

    }
     */

}
