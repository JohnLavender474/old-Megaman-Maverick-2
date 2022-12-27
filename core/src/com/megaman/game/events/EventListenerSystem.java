package com.megaman.game.events;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.System;
import com.megaman.game.entities.Entity;

import java.util.function.Consumer;

public class EventListenerSystem extends System implements EventListener {

    private final Array<Event> events = new Array<>();

    public EventListenerSystem(EventManager eventMan) {
        super(EventListenerComponent.class);
        eventMan.add(this);
    }

    @Override
    public void listenForEvent(Event event) {
        events.add(event);
    }

    @Override
    protected void processEntity(Entity e, float delta) {
        EventListenerComponent c = e.getComponent(EventListenerComponent.class);
        for (Event event : events) {
            for (Consumer<Event> eventConsumer : c.eventConsumers) {
                eventConsumer.accept(event);
            }
        }
    }

    @Override
    protected void postProcess(float delta) {
        events.clear();
    }

}
