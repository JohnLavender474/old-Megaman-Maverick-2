package com.megaman.game.cull;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.events.EventManager;

import java.util.LinkedList;
import java.util.Queue;

public class CullOnEventSystem extends System implements EventListener {

    private final Array<Event> events;
    private final Queue<Event> eventsToAdd;

    public CullOnEventSystem(EventManager eventMan) {
        super(CullOnEventComponent.class);
        eventMan.add(this);
        events = new Array<>();
        eventsToAdd = new LinkedList<>();
    }

    @Override
    public void listenForEvent(Event event) {
        if (updating) {
            eventsToAdd.add(event);
        } else {
            events.add(event);
        }
    }

    @Override
    protected void preProcess(float delta) {
        while (!eventsToAdd.isEmpty()) {
            events.add(eventsToAdd.poll());
        }
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        CullOnEventComponent c = e.getComponent(CullOnEventComponent.class);
        for (Event event : events) {
            if (c.isCullEvent(event)) {
                e.dead = true;
                break;
            }
        }
    }

    @Override
    protected void postProcess(float delta) {
        events.clear();
    }

}
