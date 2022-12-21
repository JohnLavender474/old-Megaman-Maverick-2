package com.megaman.game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedSet;
import com.megaman.game.entities.Entity;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public abstract class System implements Updatable, Resettable {

    protected final Array<Entity> entities = new Array<>(150);
    protected final Queue<Entity> entitiesToAddQueue = new LinkedList<>();
    protected final OrderedSet<Class<? extends Component>> componentMask = new OrderedSet<>();

    protected boolean on = true;
    protected boolean updating;

    @SafeVarargs
    public System(Class<? extends Component>... componentMask) {
        this(Arrays.asList(componentMask));
    }

    public System(Iterable<Class<? extends Component>> componentMask) {
        for (Class<? extends Component> c : componentMask) {
            this.componentMask.add(c);
        }
    }

    protected abstract void processEntity(Entity e, float delta);

    protected void preProcess(float delta) {
    }

    protected void postProcess(float delta) {
    }

    public void purgeAllEntities() {
        if (updating) {
            throw new IllegalStateException("Cannot purge entities while system is updating");
        }
        entities.clear();
    }

    public void addEntity(Entity e) {
        entitiesToAddQueue.add(e);
    }

    public boolean qualifiesMembership(Entity e) {
        for (Class<? extends Component> mask : componentMask) {
            if (!e.hasComponent(mask)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void update(float delta) {
        if (!on) {
            updating = false;
            return;
        }
        updating = true;
        while (!entitiesToAddQueue.isEmpty()) {
            Entity e = entitiesToAddQueue.poll();
            entities.add(e);
        }
        preProcess(delta);
        Iterator<Entity> eIter = entities.iterator();
        while (eIter.hasNext()) {
            Entity e = eIter.next();
            if (e.dead || !qualifiesMembership(e)) {
                eIter.remove();
            } else {
                processEntity(e, delta);
            }
        }
        postProcess(delta);
        updating = false;
    }

    @Override
    public void reset() {
        entities.clear();
        entitiesToAddQueue.clear();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
