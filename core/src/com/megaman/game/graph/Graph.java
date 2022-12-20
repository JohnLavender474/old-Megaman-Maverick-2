package com.megaman.game.graph;

import com.badlogic.gdx.utils.Array;

import java.util.function.Consumer;

public class Graph<N extends Node> {

    public final int width;
    public final int height;
    public final N[][] nodes;

    public Graph(N[][] nodes) {
        this.nodes = nodes;
        this.width = nodes.length;
        this.height = nodes[0].length;
    }

    public N getNode(int x, int y) {
        if (x < 0 || x >= nodes.length || y < 0 || y >= nodes[0].length) {
            return null;
        }
        return nodes[x][y];
    }

    public Array<N> getNeighbors(Node node, boolean diagonal) {
        int x = node.x;
        int y = node.y;
        Array<N> neighbors = new Array<>(8);
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if ((i == x && j == y) || isOutOfBounds(i, y)) {
                    continue;
                }
                if (!diagonal && (i == x - 1 || i == x + 1) && (j == y - 1 || j == y + 1)) {
                    continue;
                }
                neighbors.add(nodes[i][j]);
            }
        }
        return neighbors;
    }

    public boolean isOutOfBounds(int row, int col) {
        return row < 0 || col < 0 || row >= nodes.length || col >= nodes[0].length;
    }

    public void forEachNode(Consumer<N> nodeConsumer) {
        for (N[] nodeRow : nodes) {
            for (N node : nodeRow) {
                nodeConsumer.accept(node);
            }
        }
    }

}

