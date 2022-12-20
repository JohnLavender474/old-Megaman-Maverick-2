package com.megaman.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.OrderedSet;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class GameEngine implements Updatable, Resettable {

    private final OrderedMap<Class<? extends System>, System> systems = new OrderedMap<>();
    private final OrderedSet<Entity> entities = new OrderedSet<>(200);
    private final Queue<Entity> entitiesToAdd = new LinkedList<>();

    private boolean purge;
    private boolean updating;

    public GameEngine(Iterable<System> systems) {
        for (System s : systems) {
            this.systems.put(s.getClass(), s);
        }
    }

    @SafeVarargs
    public final void setSystemsOn(boolean on, Class<? extends System>... sClasses) {
        for (Class<? extends System> sClass : sClasses) {
            if (!systems.containsKey(sClass)) {
                continue;
            }
            getSystem(sClass).on = on;
        }
    }

    public <S extends System> S getSystem(Class<S> sClass) {
        return sClass.cast(systems.get(sClass));
    }

    public void spawnEntity(Entity e) {
        entitiesToAdd.add(e);
    }

    public void spawnEntity(Entity e, Vector2 spawn) {
        spawnEntity(e, spawn, new ObjectMap<>());
    }

    public void spawnEntity(Entity e, Vector2 spawn, ObjectMap<String, Object> data) {
        e.init(spawn, data);
        entitiesToAdd.add(e);
    }

    public void spawnEntity(Entity e, Rectangle bounds) {
        spawnEntity(e, bounds, new ObjectMap<>());
    }

    public void spawnEntity(Entity e, Rectangle bounds, ObjectMap<String, Object> data) {
        e.init(bounds, data);
        entitiesToAdd.add(e);
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
            }
        }
        while (!entitiesToAdd.isEmpty()) {
            Entity e = entitiesToAdd.poll();
            e.dead = false;
            entities.add(e);
            for (System s : systems.values()) {
                if (s.qualifiesMembership(e)) {
                    s.addEntity(e);
                }
            }
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
