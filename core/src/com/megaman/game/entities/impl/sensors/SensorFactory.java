package com.megaman.game.entities.impl.sensors;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.factories.EntityFactory;
import com.megaman.game.entities.factories.EntityPool;
import com.megaman.game.entities.impl.sensors.impl.Death;
import com.megaman.game.entities.impl.sensors.impl.Gate;

public class SensorFactory implements EntityFactory {

    public static final String GATE = "Gate";
    public static final String DEATH = "Death";

    private final ObjectMap<String, EntityPool> pools;

    public SensorFactory(MegamanGame game) {
        pools = new ObjectMap<>() {{
            put(GATE, new EntityPool(5, () -> new Gate(game)));
            put(DEATH, new EntityPool(25, () -> new Death(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
