package com.megaman.game.events;

import com.badlogic.gdx.utils.ObjectMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Event {

    public final EventType type;
    public final ObjectMap<String, Object> info;

    public Event(EventType type) {
        this(type, new ObjectMap<>());
    }

    public boolean hasInfo(String key) {
        return info.containsKey(key);
    }

    public Object getInfo(String key) {
        return info.get(key);
    }

    public <T> T getInfo(String key, Class<T> tClass) {
        return tClass.cast(getInfo(key));
    }

}
