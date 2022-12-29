package com.megaman.game.events;

import com.badlogic.gdx.utils.OrderedSet;

import java.util.LinkedList;
import java.util.Queue;

public class EventManager implements Runnable {

    private final OrderedSet<EventListener> eventListeners;
    private final Queue<Event> eventQueue;

    public EventManager() {
        eventListeners = new OrderedSet<>();
        eventQueue = new LinkedList<>();
    }

    public void submit(Event event) {
        eventQueue.add(event);
    }

    public void add(EventListener e) {
        eventListeners.add(e);
    }

    public void remove(EventListener e) {
        eventListeners.remove(e);
    }

    @Override
    public void run() {
        while (!eventQueue.isEmpty()) {
            Event e = eventQueue.poll();
            for (EventListener l : eventListeners) {
                l.listenForEvent(e);
            }
        }
    }

}
