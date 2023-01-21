package com.megaman.game.entities.impl.bosses;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.impl.bosses.impl.GutsTank;
import com.megaman.game.entities.utils.factories.EntityFactory;
import com.megaman.game.entities.utils.factories.EntityPool;

public class BossFactory implements EntityFactory {

    public static final String GUTS_TANK = "GutsTank";

    private final ObjectMap<String, EntityPool> pool;

    public BossFactory(MegamanGame game) {
        pool = new ObjectMap<>() {{
            put(GUTS_TANK, new EntityPool(1, () -> new GutsTank(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pool.get(key).fetch();
    }

}
