package com.megaman.game.entities.sensors;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;

public class SensorFactory implements EntityFactory {

    public static final String DEATH = "Death";

    private final ObjectMap<String, EntityPool> pools;

    public SensorFactory(MegamanGame game) {
        pools = new ObjectMap<>() {{
            put(DEATH, new EntityPool(25, () -> new DeathSensor(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
