package com.megaman.game.events;

import com.badlogic.gdx.utils.ObjectMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Event {

    public final EventType eventType;
    public final ObjectMap<String, Object> info;

    public Event(EventType eventType) {
        this(eventType, new ObjectMap<>());
    }

    public <T> T getInfo(String key, Class<T> tClass) {
        return tClass.cast(info.get(key));
    }

}
