package com.megaman.game.events;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;

import java.util.function.Consumer;

public class EventListenerComponent implements Component {

    public Array<Consumer<Event>> eventConsumers = new Array<>();

    @SafeVarargs
    public EventListenerComponent(Consumer<Event>... eventConsumers) {
        this.eventConsumers.addAll(eventConsumers);
    }

    public void add(Consumer<Event> eventConsumer) {
        eventConsumers.add(eventConsumer);
    }

}
