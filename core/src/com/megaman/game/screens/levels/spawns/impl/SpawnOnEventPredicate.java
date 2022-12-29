package com.megaman.game.screens.levels.spawns.impl;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.screens.levels.spawns.Spawn;
import com.megaman.game.utils.Logger;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SpawnOnEventPredicate extends Spawn implements EventListener {

    private static final Logger logger = new Logger(SpawnOnEventPredicate.class, MegamanGame.DEBUG && false);

    private final Predicate<Event> pred;
    private final Queue<Event> q;

    private boolean predPassed;

    public SpawnOnEventPredicate(GameEngine engine, Rectangle bounds, ObjectMap<String, Object> data,
                                 Supplier<Entity> entitySupplier, Predicate<Event> pred) {
        super(engine, bounds, data, entitySupplier);
        this.pred = pred;
        q = new LinkedList<>();
    }

    @Override
    public void update(float delta) {
        if (entity != null && entity.dead) {
            logger.log("Cull: " + entity);
            entity = null;
            predPassed = false;
        }
        while (!q.isEmpty()) {
            if (pred.test(q.poll())) {
                predPassed = true;
                break;
            }
        }
        q.clear();
        if (entity == null && predPassed) {
            spawnEntity();
        }
    }

    @Override
    public void listenForEvent(Event event) {
        q.add(event);
    }

}
