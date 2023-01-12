package com.megaman.game.entities.impl.items;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.factories.EntityFactory;
import com.megaman.game.entities.factories.EntityPool;
import com.megaman.game.entities.impl.items.impl.HealthBulb;
import com.megaman.game.entities.impl.items.impl.HeartTank;

public class ItemFactory implements EntityFactory {

    public static final String HEALTH_BULB = "HealthBulb";
    public static final String ARMOR_PIECE = "ArmorPiece";
    public static final String HEALTH_TANK = "HealthTank";
    public static final String HEART_TANK = "HeartTank";

    private final ObjectMap<String, EntityPool> pools;

    public ItemFactory(MegamanGame game) {
        pools = new ObjectMap<>() {{
            put(HEALTH_BULB, new EntityPool(5, () -> new HealthBulb(game)));
            put(HEART_TANK, new EntityPool(8, () -> new HeartTank(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
