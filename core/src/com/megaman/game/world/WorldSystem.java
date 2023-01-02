package com.megaman.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedSet;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import lombok.Setter;

import java.util.*;

public class WorldSystem extends System {

    private static final int PROCESS_CYCLES = 2;

    private static final Map<FixtureType, Set<FixtureType>> masks = new EnumMap<>(FixtureType.class) {{
        put(FixtureType.CONSUMER, EnumSet.allOf(FixtureType.class));
        put(FixtureType.PLAYER, EnumSet.of(
                FixtureType.ITEM));
        put(FixtureType.DAMAGEABLE, EnumSet.of(
                FixtureType.DEATH,
                FixtureType.DAMAGER));
        put(FixtureType.BODY, EnumSet.of(
                FixtureType.FORCE));
        put(FixtureType.WATER_LISTENER, EnumSet.of(
                FixtureType.WATER));
        put(FixtureType.LADDER, EnumSet.of(
                FixtureType.HEAD,
                FixtureType.FEET));
        put(FixtureType.SIDE, EnumSet.of(
                FixtureType.ICE,
                FixtureType.GATE,
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
                FixtureType.SHIELD,
                FixtureType.WATER));
        put(FixtureType.LASER, EnumSet.of(
                FixtureType.BLOCK));
    }};

    private static boolean filter(Fixture f1, Fixture f2) {
        if (f1.entity.equals(f2.entity)) {
            return false;
        }
        return (masks.containsKey(f1.fixtureType) && masks.get(f1.fixtureType).contains(f2.fixtureType)) ||
                (masks.containsKey(f2.fixtureType) && masks.get(f2.fixtureType).contains(f1.fixtureType));
    }

    private final WorldContactListener contactListener;
    private final SpecialCollisionHandler specialCollisionHandler;

    private OrderedSet<Contact> priorContacts;
    private OrderedSet<Contact> currContacts;
    private float accumulator;
    private int currCycle;

    @Setter
    private WorldGraph worldGraph;

    public WorldSystem(WorldContactListener contactListener, SpecialCollisionHandler specialCollisionHandler) {
        super(BodyComponent.class);
        this.contactListener = contactListener;
        this.specialCollisionHandler = specialCollisionHandler;
        priorContacts = new OrderedSet<>();
        currContacts = new OrderedSet<>();
    }

    @Override
    protected void preProcess(float delta) {
        worldGraph.reset();
        while (!entitiesToAddQueue.isEmpty()) {
            entities.add(entitiesToAddQueue.poll());
        }
        Iterator<Entity> eIter = entities.iterator();
        while (eIter.hasNext()) {
            Entity e = eIter.next();
            if (e.dead || !qualifiesMembership(e)) {
                eIter.remove();
            }
            Body body = e.getComponent(BodyComponent.class).body;
            body.setPrevPos(body.bounds.x, body.bounds.y);
            if (body.preProcess != null) {
                body.preProcess.update(delta);
            }
        }
    }

    @Override
    public void update(float delta) {
        if (!on) {
            updating = false;
            return;
        }
        updating = true;
        accumulator += delta;
        while (accumulator >= WorldVals.FIXED_STEP) {
            accumulator -= WorldVals.FIXED_STEP;
            currCycle = 0;
            preProcess(delta);
            while (currCycle < PROCESS_CYCLES) {
                for (Entity e : entities) {
                    if (e.asleep) {
                        continue;
                    }
                    processEntity(e, WorldVals.FIXED_STEP);
                }
                currCycle++;
            }
            postProcess(delta);
            for (Contact c : currContacts) {
                if (priorContacts.contains(c)) {
                    contactListener.continueContact(c, delta);
                } else {
                    contactListener.beginContact(c, delta);
                }
            }
            for (Contact c : priorContacts) {
                if (!currContacts.contains(c)) {
                    contactListener.endContact(c, delta);
                }
            }
            priorContacts = currContacts;
            currContacts = new OrderedSet<>();
            worldGraph.reset();
        }
        /*
        for (Contact c : currContacts) {
            if (priorContacts.contains(c)) {
                contactListener.continueContact(c, delta);
            } else {
                contactListener.beginContact(c, delta);
            }
        }
        for (Contact c : priorContacts) {
            if (!currContacts.contains(c)) {
                contactListener.endContact(c, delta);
            }
        }
        priorContacts = currContacts;
        currContacts = new OrderedSet<>();
         */
        updating = false;
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        Body body = e.getComponent(BodyComponent.class).body;
        switch (currCycle) {
            case 0 -> updateBody(body, delta);
            case 1 -> resolve(body);
        }
    }

    @Override
    protected void postProcess(float delta) {
        Iterator<Entity> eIter = entities.iterator();
        while (eIter.hasNext()) {
            Entity e = eIter.next();
            if (e.dead || !qualifiesMembership(e)) {
                eIter.remove();
            }
            Body body = e.getComponent(BodyComponent.class).body;
            worldGraph.addBody(body);
            for (Fixture f : body.fixtures) {
                worldGraph.addFixture(f);
            }
            if (body.postProcess != null) {
                body.postProcess.update(delta);
            }
        }
    }

    private void updateBody(Body body, float delta) {
        body.update(delta);
        addToGraph(body);
    }

    private void addToGraph(Body body) {
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
        if (body.is(BodyType.ABSTRACT) || body.is(BodyType.STATIC)) {
            return;
        }
        Array<Body> overlapping = worldGraph.getBodiesOverlapping(body, o -> o.is(BodyType.STATIC) && o.collisionOn);
        for (Body o : overlapping) {
            handleCollision(body, o);
        }
    }

    private void handleCollision(Body dynamicBody, Body staticBody) {
        if (dynamicBody.bodyType != BodyType.DYNAMIC || staticBody.bodyType != BodyType.STATIC) {
            throw new IllegalStateException("First body must be dynamic, second must be static");
        }
        Rectangle overlap = new Rectangle();
        if (!dynamicBody.intersects(staticBody, overlap)) {
            return;
        }
        if (specialCollisionHandler.handleSpecial(dynamicBody, staticBody, overlap)) {
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
