package com.megaman.game.graph;

import com.badlogic.gdx.math.Vector2;
import com.megaman.game.utils.interfaces.Positional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Node implements Positional, Comparable<Node> {

    public final int x;
    public final int y;

    @Override
    public Vector2 getPos() {
        return new Vector2(x, y);
    }

    @Override
    public int compareTo(Node o) {
        int comp = x - o.x;
        if (comp == 0) {
            comp = y - o.y;
        }
        return comp;
    }

    @Override
    public int hashCode() {
        int hash = 49;
        hash *= 7 + x;
        hash *= 7 + y;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Node n && x == n.x && y == n.y;
    }

}