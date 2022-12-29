package com.megaman.game.entities.items;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;
import com.megaman.game.entities.items.impl.HealthBulb;

public class ItemFactory implements EntityFactory {

    public static final String HEALTH_BULB = "HealthBulb";

    private final ObjectMap<String, EntityPool> pools;

    public ItemFactory(MegamanGame game) {
        pools = new ObjectMap<>() {{
            put(HEALTH_BULB, new EntityPool(5, () -> new HealthBulb(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
