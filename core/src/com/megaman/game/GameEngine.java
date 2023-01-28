package com.megaman.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.OrderedSet;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.KeyValuePair;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class GameEngine implements Updatable, Resettable {

    private static final Logger logger = new Logger(GameEngine.class, MegamanGame.DEBUG && false);

    private final OrderedMap<Class<? extends System>, System> systems = new OrderedMap<>();

    private final OrderedSet<Entity> entities = new OrderedSet<>(200);
    private final Queue<KeyValuePair<Entity, Runnable>> entitiesToAdd = new LinkedList<>();

    private boolean purge;
    private boolean updating;

    public GameEngine(System... systems) {
        this(new Array<>(systems));
    }

    public GameEngine(Iterable<System> systems) {
        for (System s : systems) {
            this.systems.put(s.getClass(), s);
        }
    }

    public OrderedMap<Class<? extends System>, Boolean> getStates() {
        OrderedMap<Class<? extends System>, Boolean> sysStates = new OrderedMap<>();
        for (ObjectMap.Entry<Class<? extends System>, System> e : systems.entries()) {
            boolean on = e.value.on;
            sysStates.put(e.key, on);
        }
        return sysStates;
    }

    public void set(OrderedMap<Class<? extends System>, Boolean> sysStates) {
        for (ObjectMap.Entry<Class<? extends System>, Boolean> e : sysStates) {
            set(e.value, e.key);
        }
    }

    public final void setAll(boolean on) {
        for (System s : systems.values()) {
            s.on = on;
        }
    }

    @SafeVarargs
    public final void set(boolean on, Class<? extends System>... sClasses) {
        for (Class<? extends System> sClass : sClasses) {
            if (!systems.containsKey(sClass)) {
                throw new IllegalStateException("Not in engine: " + sClass);
            }
            getSystem(sClass).on = on;
        }
    }

    public <S extends System> S getSystem(Class<S> sClass) {
        return sClass.cast(systems.get(sClass));
    }

    public void spawn(Entity e) {
        spawn(e, new Vector2());
    }

    public void spawn(Entity e, ObjectMap<String, Object> data) {
        spawn(e, new Vector2(), data);
    }

    public void spawn(Entity e, Vector2 spawn) {
        spawn(e, spawn, new ObjectMap<>());
    }

    public void spawn(Entity e, Vector2 spawn, ObjectMap<String, Object> data) {
        entitiesToAdd.add(KeyValuePair.of(e, () -> e.init(spawn, data)));
    }

    public void spawn(Entity e, Rectangle bounds) {
        spawn(e, bounds, new ObjectMap<>());
    }

    public void spawn(Entity e, Rectangle bounds, ObjectMap<String, Object> data) {
        entitiesToAdd.add(KeyValuePair.of(e, () -> e.init(bounds, data)));
    }

    @Override
    public void update(float delta) {
        updating = true;
        if (purge) {
            purge();
        }
        Iterator<Entity> eIter = entities.iterator();
        while (eIter.hasNext()) {
            Entity e = eIter.next();
            if (e.dead) {
                for (Runnable r : e.runOnDeath) {
                    r.run();
                }
                eIter.remove();
                logger.log("Removed from engine: " + e);
            }
        }
        while (!entitiesToAdd.isEmpty()) {
            KeyValuePair<Entity, Runnable> p = entitiesToAdd.poll();
            Entity e = p.key();
            e.dead = false;
            for (Component c : e.components.values()) {
                c.reset();
            }
            p.value().run();
            entities.add(e);
            for (System s : systems.values()) {
                if (s.qualifiesMembership(e)) {
                    s.addEntity(e);
                }
            }
            logger.log("Added to engine: " + e);
        }
        for (System s : systems.values()) {
            s.update(delta);
        }
        updating = false;
    }

    private void purge() {
        for (Entity e : entities) {
            for (Runnable r : e.runOnDeath) {
                r.run();
            }
            e.dead = true;
        }
        entities.clear();
        for (System s : systems.values()) {
            s.purgeAllEntities();
        }
        purge = false;
    }

    @Override
    public void reset() {
        if (updating) {
            purge = true;
            return;
        }
        purge();
    }


}
