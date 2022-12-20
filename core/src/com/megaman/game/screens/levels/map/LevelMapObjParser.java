package com.megaman.game.screens.levels.map;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Iterator;

public class LevelMapObjParser {

    public static ObjectMap<String, Object> parse(RectangleMapObject obj) {
        ObjectMap<String, Object> m = new ObjectMap<>();
        MapProperties props = obj.getProperties();
        Iterator<String> keyIter = props.getKeys();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            Object o = props.get(key);
            m.put(key, o);
        }
        return m;
    }

}
