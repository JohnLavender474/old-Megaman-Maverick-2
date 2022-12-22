package com.megaman.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedSet;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;

import java.util.*;

public class WorldSystem extends System {

    private static final int PROCESS_CYCLES = 2;

    private static final Map<FixtureType, Set<FixtureType>> masks = new EnumMap<>(FixtureType.class) {{
        put(FixtureType.DAMAGER, EnumSet.of(
                FixtureType.DAMAGEABLE));
        put(FixtureType.BODY, EnumSet.of(
                FixtureType.WATER,
                FixtureType.FORCE));
        put(FixtureType.SIDE, EnumSet.of(
                FixtureType.ICE,
                FixtureType.BLOCK,
                FixtureType.BOUNCER));
        put(FixtureType.FEET, EnumSet.of(
                FixtureType.ICE,
                FixtureType.BLOCK,
                FixtureType.BOUNCER));
        put(FixtureType.HEAD, EnumSet.of(
                FixtureType.BLOCK,
                FixtureType.BOUNCER));
        put(FixtureType.PROJECTILE, EnumSet.of(
                FixtureType.BODY,
                FixtureType.BLOCK,
                FixtureType.SHIELD));
        put(FixtureType.LASER, EnumSet.of(
                FixtureType.BLOCK));
    }};

    private static boolean filter(Fixture f1, Fixture f2) {
        return (masks.containsKey(f1.fixtureType) && masks.get(f1.fixtureType).contains(f2.fixtureType)) ||
                (masks.containsKey(f2.fixtureType) && masks.get(f2.fixtureType).contains(f1.fixtureType));
    }

    private final WorldContactListener listener;

    private OrderedSet<Contact> priorContacts;
    private OrderedSet<Contact> currContacts;
    private WorldGraph worldGraph;
    private float accumulator;
    private int currCycle;

    public WorldSystem(WorldContactListener listener) {
        super(BodyComponent.class);
        this.listener = listener;
        this.priorContacts = new OrderedSet<>();
        this.currContacts = new OrderedSet<>();
    }

    public void setWorldSize(int worldWidth, int worldHeight) {
        worldGraph = new WorldGraph(worldWidth, worldHeight);
    }

    @Override
    protected void preProcess(float delta) {
        while (!entitiesToAddQueue.isEmpty()) {
            entities.add(entitiesToAddQueue.poll());
        }
        Iterator<Entity> eIter = entities.iterator();
        while (eIter.hasNext()) {
            Entity e = eIter.next();
            if (e.dead || !qualifiesMembership(e)) {
                eIter.remove();
                continue;
            }
            BodyComponent c = e.getComponent(BodyComponent.class);
            c.body.setPrevPos(new Vector2(c.body.bounds.x, c.body.bounds.y));
        }
    }

    @Override
    public void update(float delta) {
        if (!on) {
            updating = false;
            return;
        }
        updating = true;
        preProcess(delta);
        accumulator += delta;
        while (accumulator >= WorldVals.FIXED_STEP) {
            accumulator -= WorldVals.FIXED_STEP;
            currCycle = 0;
            while (currCycle < PROCESS_CYCLES) {
                for (Entity e : entities) {
                    if (e.asleep) {
                        continue;
                    }
                    processEntity(e, WorldVals.FIXED_STEP);
                }
                currCycle++;
            }
            worldGraph.reset();
        }
        for (Contact c : currContacts) {
            if (priorContacts.contains(c)) {
                listener.continueContact(c, delta);
            } else {
                listener.beginContact(c, delta);
            }
        }
        for (Contact c : priorContacts) {
            if (!currContacts.contains(c)) {
                listener.endContact(c, delta);
            }
        }
        priorContacts = currContacts;
        currContacts = new OrderedSet<>();
        updating = false;
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        BodyComponent bc = e.getComponent(BodyComponent.class);
        switch (currCycle) {
            case 0 -> updateBody(bc.body, delta);
            case 1 -> resolve(bc.body);
        }

    }

    private void updateBody(Body body, float delta) {
        body.update(delta);
        worldGraph.addBody(body);
        for (Fixture f : body.fixtures) {
            if (!f.active) {
                continue;
            }
            worldGraph.addFixture(f);
        }
    }

    private void resolve(Body body) {
        for (Fixture f : body.fixtures) {
            if (!masks.containsKey(f.fixtureType)) {
                continue;
            }
            Array<Fixture> overlapping = worldGraph.getFixturesOverlapping(f, o -> filter(f, o));
            for (Fixture o : overlapping) {
                currContacts.add(new Contact(f, o));
            }
        }
        if (body.bodyType == BodyType.ABSTRACT || body.bodyType == BodyType.STATIC) {
            return;
        }
        Array<Body> overlapping = worldGraph.getBodiesOverlapping(body, o -> o.bodyType == BodyType.STATIC);
        for (Body o : overlapping) {
            handleCollision(body, o);
        }
    }

    private void handleCollision(Body dynamicBody, Body staticBody) {
        if (dynamicBody.bodyType != BodyType.DYNAMIC || staticBody.bodyType != BodyType.STATIC) {
            throw new IllegalStateException("First body must be dynamic, second must be static");
        }
        Rectangle overlap = new Rectangle();
        boolean overlapping = dynamicBody.intersects(staticBody, overlap);
        if (!overlapping) {
            return;
        }
        if (overlap.width > overlap.height) {
            dynamicBody.resistance.x += staticBody.friction.x;
            if (dynamicBody.bounds.y > staticBody.bounds.y) {
                dynamicBody.bounds.y += overlap.height;
            } else {
                dynamicBody.bounds.y -= overlap.height;
            }
        } else {
            dynamicBody.resistance.y += staticBody.friction.y;
            if (dynamicBody.bounds.x > staticBody.bounds.x) {
                dynamicBody.bounds.x += overlap.width;
            } else {
                dynamicBody.bounds.x -= overlap.width;
            }
        }
    }

}
