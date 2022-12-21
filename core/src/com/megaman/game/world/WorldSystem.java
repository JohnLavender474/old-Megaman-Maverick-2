package com.megaman.game.world;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedSet;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.enums.FloatToInt;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.KeyValuePair;
import com.megaman.game.utils.objs.Pair;
import lombok.*;

import java.util.*;

import static com.megaman.game.utils.enums.FloatToInt.CEIL;
import static com.megaman.game.utils.enums.FloatToInt.FLOOR;
import static com.megaman.game.world.WorldVals.PPM;
import static java.lang.Math.*;

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

    // optimize by creating contact instances only when passing this filter method
    private static boolean filter(Fixture f1, Fixture f2) {
        return masks.containsKey(f1.fixtureType) && masks.get(f1.fixtureType).contains(f2.fixtureType);
    }

    private final WorldContactListener listener;

    private OrderedSet<Contact> priorContacts = new OrderedSet<>();
    private OrderedSet<Contact> currContacts = new OrderedSet<>();
    private WorldGraph worldGraph;
    private float accumulator;
    private int currCycle;

    public WorldSystem(WorldContactListener listener) {
        super(BodyComponent.class);
        this.listener = listener;
    }

    public void setWorldGraph(int worldWidth, int worldHeight) {
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
            // set prev pos of body
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
        // step world by fixed time
        accumulator += delta;
        while (accumulator >= WorldVals.FIXED_STEP) {
            // subtract fixed step
            accumulator -= WorldVals.FIXED_STEP;
            // reset cycle to 0
            currCycle = 0;
            // cycle 0: update bodies
            // cycle 1: resolve bodies
            while (currCycle < PROCESS_CYCLES) {
                for (Entity e : entities) {
                    processEntity(e, WorldVals.FIXED_STEP);
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
        // abstract bodies do not need collision handling
        if (body.bodyType == BodyType.ABSTRACT) {
            return;
        }
        Array<Body> overlapping = worldGraph.getBodiesOverlapping(body,
                o -> body.bodyType == BodyType.STATIC ? o.bodyType == BodyType.DYNAMIC : o.bodyType == BodyType.STATIC);
        if (body.bodyType == BodyType.STATIC) {
            for (Body o : overlapping) {
                handleCollision(o, body);
            }
        } else {
            for (Body o : overlapping) {
                handleCollision(body, o);
            }
        }         
    }

    private void handleCollision(Body dynamicBody, Body staticBody) {
        if (dynamicBody.bodyType != BodyType.DYNAMIC || staticBody.bodyType != BodyType.STATIC) {
            throw new IllegalStateException("First body must be dynamic, second must be static");
        }
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

/*

public class WorldSystem extends System {
    
    private static final float MIN_VEL = .01f;

    private final Set<Contact> priorContacts = new HashSet<>();
    private final Set<Contact> currentContacts = new HashSet<>();
    private final List<Body> bodies = new ArrayList<>();
    private final WorldContactListener worldContactListener;

    private Vector2 airResistance = new Vector2(1.035f, 1.025f);
    private float accumulator;

    public WorldSystem(WorldContactListener worldContactListener) {
        super(BodyComponent.class);
        this.worldContactListener = worldContactListener;
    }

    @Override
    protected void preProcess(float delta) {
        bodies.clear();
    }

    @Override
    protected void processEntity(Entity entity, float delta) {
        BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);
        bodies.add(bodyComponent.body);
    }

    @Override
    protected void postProcess(float delta) {
        accumulator += delta;
        float fixedTimeStep = 1f / 150f;
        while (accumulator >= fixedTimeStep) {
            accumulator -= fixedTimeStep;
            for (Body body : bodies) {
                Vector2 velocity = body.velocity;
                // set velocity to zero if below threshold
                if (abs(velocity.x) < PPM * MIN_VEL) {
                    velocity.x = 0f;
                }
                if (abs(velocity.y) < PPM * MIN_VEL) {
                    velocity.y = 0f;
                }
                // apply resistance
                if (body.affectedByResistance) {
                    if (body.resistance.x != 0) {
                        velocity.x /= body.resistance.x;
                    }
                    if (body.resistance.y != 0) {
                        velocity.y /= body.resistance.y;
                    }
                }
                // reset resistance
                body.resistance.set(airResistance);
                // if gravity on, apply gravity
                if (body.gravityOn) {
                    velocity.add(0f, body.gravity.y);
                }
                // clamp velocity
                Vector2 clamp = body.velClamp;
                if (velocity.x > 0f && velocity.x > abs(clamp.x)) {
                    velocity.x = abs(clamp.x);
                } else if (velocity.x < 0f && velocity.x < -abs(clamp.x)) {
                    velocity.x = -abs(clamp.x);
                }
                if (velocity.y > 0f && velocity.y > abs(clamp.y)) {
                    velocity.y = abs(clamp.y);
                } else if (velocity.y < 0f && velocity.y < -abs(clamp.y)) {
                    velocity.y = -abs(clamp.y);
                }
                // translate
                body.bounds.x += velocity.x * fixedTimeStep;
                body.bounds.y += velocity.y * fixedTimeStep;
                // set fixtures
                for (Fixture fixture : body.fixtures) {
                    Vector2 center = ShapeUtils.getCenterPoint(body.bounds);
                    center.add(fixture.offset);
                    fixture.bounds.setCenter(center);
                }
            }
            // handle collisions
            for (Body b1 : bodies) {
                for (Body b2 : bodies) {
                    if (b1.equals(b2)) {
                        continue;
                    }
                    Rectangle overlap = new Rectangle();
                    if (b1.intersects(b2, overlap)) {
                        handleCollision(b1, b2, overlap);
                    }
                }
            }
            // handle fixture contacts
            for (Body b1 : bodies) {
                for (Body b2 : bodies) {
                    if (b1.equals(b2)) {
                        continue;
                    }
                    for (Fixture f1 : b1.fixtures) {
                        for (Fixture f2 : b2.fixtures) {
                            if (f1.overlaps(f2)) {
                                currentContacts.add(new Contact(f1, f2));
                            }
                        }
                    }
                }
            }
        }
        // handle contacts in the current contacts setBounds
        currentContacts.forEach(contact -> {
            if (priorContacts.contains(contact)) {
                worldContactListener.continueContact(contact, delta);
            } else {
                worldContactListener.beginContact(contact, delta);
            }
        });
        // handle contacts in the prior contacts setBounds
        priorContacts.forEach(contact -> {
            if (!currentContacts.contains(contact)) {
                worldContactListener.endContact(contact, delta);
            }
        });
        // move current contacts to prior contacts setBounds, then clear the current contacts setBounds
        priorContacts.clear();
        priorContacts.addAll(currentContacts);
        currentContacts.clear();
        bodies.forEach(b -> b.setPrevPos(ShapeUtils.getPoint(b.bounds, Position.BOTTOM_LEFT)));
    }

    private void handleCollision(Body b1, Body b2, Rectangle overlap) {
        if (overlap.getWidth() > overlap.getHeight()) {
            if (b1.bounds.getY() > b2.bounds.getY()) {
                if (ceil(b1.velocity.y) < -1f) {
                    b1.resistance.x += b2.friction.x;
                }
                if (floor(b2.velocity.y) > 1f) {
                    b2.resistance.x += b1.friction.x;
                }
                if ((b1.bodyType == BodyType.DYNAMIC && b2.bodyType == BodyType.STATIC)) {
                    b1.bounds.y += overlap.getHeight();
                } else if ((b2.bodyType == BodyType.DYNAMIC && b1.bodyType == BodyType.STATIC)) {
                    b2.bounds.y -= overlap.getHeight();
                }
            } else {
                if (floor(b1.velocity.y) > 1f) {
                    b1.resistance.x += b2.friction.x;
                }
                if (ceil(b2.velocity.y) < -1f) {
                    b2.resistance.x += b1.friction.x;
                }
                if ((b1.bodyType == BodyType.DYNAMIC && b2.bodyType == BodyType.STATIC)) {
                    b1.bounds.y -= overlap.getHeight();
                } else if ((b2.bodyType == BodyType.DYNAMIC && b1.bodyType == BodyType.STATIC)) {
                    b2.bounds.y += overlap.getHeight();
                }
            }
        } else {
            if (b1.bounds.getX() > b2.bounds.getX()) {
                if ((b1.bodyType == BodyType.DYNAMIC && b2.bodyType == BodyType.STATIC)) {
                    b1.bounds.x += overlap.getWidth();
                } else if ((b2.bodyType == BodyType.DYNAMIC && b1.bodyType == BodyType.STATIC)) {
                    b2.bounds.x -= overlap.getWidth();
                }
            } else {
                if ((b1.bodyType == BodyType.DYNAMIC && b2.bodyType == BodyType.STATIC)) {
                    b1.bounds.x -= overlap.getWidth();
                } else if ((b2.bodyType == BodyType.DYNAMIC && b1.bodyType == BodyType.STATIC)) {
                    b2.bounds.x += overlap.getWidth();
                }
            }
        }
    }

}

 */
