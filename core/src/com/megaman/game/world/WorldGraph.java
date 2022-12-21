package com.megaman.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.Predicate;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.objs.KeyValuePair;
import com.sun.nio.sctp.NotificationHandler;

public class WorldGraph implements Resettable {

    private final int width;
    private final int height;
    private final OrderedMap<WorldCoordinate, Array<Body>> bodies;
    private final OrderedMap<WorldCoordinate, Array<Fixture>> fixtures;

    public WorldGraph(int width, int height) {
        this.width = width;
        this.height = height;
        this.bodies = new OrderedMap<>();
        this.fixtures = new OrderedMap<>();
    }

    private int[] getMinsAndMaxes(Rectangle bounds) {
        int minX = (int) Math.floor(bounds.x / WorldVals.PPM);
        int minY = (int) Math.floor(bounds.y / WorldVals.PPM);
        int maxX = (int) Math.ceil((bounds.x + bounds.width) / WorldVals.PPM);
        int maxY = (int) Math.ceil((bounds.y + bounds.height) / WorldVals.PPM);
        return new int[] {
                Integer.max(0, minX),
                Integer.max(0, minY),
                Integer.min(width, maxX),
                Integer.min(height, maxY)
        };
    }

    public void addBody(Body body) {
        int[] m = getMinsAndMaxes(body.bounds);
        int xMin = m[0];
        int yMin = m[1];
        int xMax = m[2];
        int yMax = m[3];
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                WorldCoordinate c = new WorldCoordinate(x, y);
                Array<Body> b;
                if (bodies.containsKey(c)) {
                    b = bodies.get(c);
                } else {
                    b = new Array<>();
                    bodies.put(c, b);
                }
                b.add(body);
            }
        }
    }

    public void addFixture(Fixture fixture) {
        int[] m = getMinsAndMaxes(fixture.bounds);
        int xMin = m[0];
        int yMin = m[1];
        int xMax = m[2];
        int yMax = m[3];
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                WorldCoordinate c = new WorldCoordinate(x, y);
                Array<Fixture> f;
                if (fixtures.containsKey(c)) {
                    f = fixtures.get(c);
                } else {
                    f = new Array<>();
                    fixtures.put(c, f);
                }
                f.add(fixture);
            }
        }
    }

    public Array<Body> getBodiesOverlapping(Body body, Predicate<Body> pred) {
        int[] m = getMinsAndMaxes(body.bounds);
        int xMin = m[0];
        int yMin = m[1];
        int xMax = m[2];
        int yMax = m[3];
        Array<Body> overlapping = new Array<>(30);
        ObjectSet<Body> checked = new ObjectSet<>();
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                WorldCoordinate c = new WorldCoordinate(x, y);
                if (!bodies.containsKey(c)) {
                    continue;
                }
                for (Body other : bodies.get(c)) {
                    if (body.equals(other) || checked.contains(other)) {
                        continue;
                    }
                    checked.add(other);
                    if (body.overlaps(other) && pred.evaluate(other)) {
                        overlapping.add(other);
                    }
                }
            }
        }
        return overlapping;
    }

    public Array<Fixture> getFixturesOverlapping(Fixture fixture, Predicate<Fixture> pred) {
        int[] m = getMinsAndMaxes(fixture.bounds);
        int xMin = m[0];
        int yMin = m[1];
        int xMax = m[2];
        int yMax = m[3];
        Array<Fixture> overlapping = new Array<>();
        ObjectSet<Fixture> checked = new ObjectSet<>();
        for (int x = xMin; x < xMax; x++) {
            for (int y = yMin; y < yMax; y++) {
                WorldCoordinate c = new WorldCoordinate(x, y);
                if (!fixtures.containsKey(c)) {
                    continue;
                }
                for (Fixture other : fixtures.get(c)) {
                    if (checked.contains(other)) {
                        continue;
                    }
                    checked.add(other);
                    if (fixture.overlaps(other) && pred.evaluate(other)) {
                        overlapping.add(other);
                    }
                }
            }
        }
        return overlapping;
    }

    @Override
    public void reset() {
        bodies.clear();
        fixtures.clear();
    }

}
