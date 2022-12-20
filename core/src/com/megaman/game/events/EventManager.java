package com.megaman.game.events;

import com.badlogic.gdx.utils.OrderedSet;

public class EventManager {

    public final OrderedSet<EventListener> eventListeners = new OrderedSet<>();

    public void dispatchEvent(Event event) {
        for (EventListener listener : eventListeners) {
            listener.listenForEvent(event);
        }
    }

    public void add(EventListener e) {
        eventListeners.add(e);
    }

    public void remove(EventListener e) {
        eventListeners.remove(e);
    }

}
