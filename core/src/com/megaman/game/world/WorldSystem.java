package com.megaman.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;

import java.util.*;

public class WorldSystem extends System {

    private static final int PROCESS_CYCLES = 2;

    private static final Map<FixtureType, Set<FixtureType>> masks = new EnumMap<>(FixtureType.class) {{
        // damager
        put(FixtureType.DAMAGER, EnumSet.of(
                FixtureType.DAMAGEABLE));
        // body
        put(FixtureType.BODY, EnumSet.of(
                FixtureType.WATER,
                FixtureType.FORCE));
        // side
        put(FixtureType.SIDE, EnumSet.of(
                FixtureType.ICE,
                FixtureType.BLOCK,
                FixtureType.BOUNCER));
        // feet
        put(FixtureType.FEET, EnumSet.of(
                FixtureType.ICE,
                FixtureType.BLOCK,
                FixtureType.BOUNCER));
        // head
        put(FixtureType.HEAD, EnumSet.of(
                FixtureType.BLOCK,
                FixtureType.BOUNCER));
        // projectile
        put(FixtureType.PROJECTILE, EnumSet.of(
                FixtureType.BODY,
                FixtureType.BLOCK,
                FixtureType.SHIELD));
        // laser
        put(FixtureType.LASER, EnumSet.of(
                FixtureType.BLOCK));
    }};

    // optimize by creating Contact instances only for contacts passing this filter method
    private static boolean filter(Fixture f1, Fixture f2) {
        return masks.containsKey(f1.fixtureType) && masks.get(f1.fixtureType).contains(f2.fixtureType);
    }

    private final WorldContactListener listener;

    private ObjectSet<Contact> priorContacts = new ObjectSet<>();
    private ObjectSet<Contact> currContacts = new ObjectSet<>();
    private WorldGraph worldGraph;
    private float accumulator;
    private int currCycle;

    public WorldSystem(WorldContactListener listener) {
        super(BodyComponent.class);
        this.listener = listener;
    }

    public void setWorldGraph(WorldGraph worldGraph) {
        this.worldGraph = worldGraph;
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
            // set prev pos of body
            BodyComponent c = e.getComponent(BodyComponent.class);
            Vector2 prevPos = new Vector2(c.body.bounds.x, c.body.bounds.y);
            c.body.setPrevPos(prevPos);
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
        // step world by fixed time
        while (accumulator >= WorldConstVals.FIXED_STEP) {
            // subtract fixed step
            accumulator -= WorldConstVals.FIXED_STEP;
            // reset cycle to 0
            currCycle = 0;
            // cycle 0: update bodies
            // cycle 1: resolve bodies
            while (currCycle < PROCESS_CYCLES) {
                for (Entity e : entities) {
                    processEntity(e, WorldConstVals.FIXED_STEP);
                }
                currCycle++;
            }
            // reset world graph
            worldGraph.reset();
        }
        // continue or begin contacts
        for (Contact c : currContacts) {
            if (priorContacts.contains(c)) {
                listener.continueContact(c, delta);
            } else {
                listener.beginContact(c, delta);
            }
        }
        // end contacts
        for (Contact c : priorContacts) {
            if (!currContacts.contains(c)) {
                listener.endContact(c, delta);
            }
        }
        // set curr contacts to prior, clear curr
        priorContacts = currContacts;
        currContacts = new ObjectSet<>();
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
            Array<Fixture> overlappingFixtures = worldGraph.getFixturesOverlapping(f);
            for (Fixture o : overlappingFixtures) {
                if (!filter(f, o)) {
                    continue;
                }
                currContacts.add(new Contact(f, o));
            }
        }
        // abstract bodies do not need collision handling
        if (body.bodyType == BodyType.ABSTRACT) {
            return;
        }
        // check for body collisions
        Array<Body> overlappingBodies = worldGraph.getBodiesOverlapping(body);
        if (body.bodyType == BodyType.STATIC) {
            for (Body o : overlappingBodies) {
                if (o.bodyType != BodyType.DYNAMIC) {
                    continue;
                }
                handleCollision(o, body);
            }
        } else {
            for (Body o : overlappingBodies) {
                if (o.bodyType != BodyType.STATIC) {
                    continue;
                }
                handleCollision(body, o);
            }
        }
    }

    private void handleCollision(Body dynamicBody, Body staticBody) {
        Rectangle overlap = new Rectangle();
        boolean overlapping = Body.intersect(dynamicBody, staticBody, overlap);
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
