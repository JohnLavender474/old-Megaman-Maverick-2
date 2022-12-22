package com.megaman.game.screens.levels.spawns.impl;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.GameEngine;
import com.megaman.game.entities.Entity;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.screens.levels.spawns.Spawn;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class SpawnOnEventPredicate extends Spawn implements EventListener {

    private final Predicate<Event> eventPredicate;

    private boolean predicatePassed;

    public SpawnOnEventPredicate(GameEngine engine,
                                 Predicate<Event> eventPredicate,
                                 Rectangle bounds,
                                 ObjectMap<String, Object> data,
                                 Supplier<Entity> entitySupplier) {
        super(engine, bounds, data, entitySupplier);
        this.eventPredicate = eventPredicate;
    }

    @Override
    public void update(float delta) {
        if (entity != null && entity.dead) {
            entity = null;
        }
        if (entity == null && predicatePassed) {
            predicatePassed = false;
            entity = entitySupplier.get();
            engine.spawnEntity(entity, bounds, data);
        }
    }

    @Override
    public void listenForEvent(Event event) {
        if (predicatePassed) {
            return;
        }
        if (eventPredicate.test(event)) {
            predicatePassed = true;
        }
    }

}
