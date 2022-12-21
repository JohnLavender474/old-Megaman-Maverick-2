package com.megaman.game.entities.special;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;

public class SpecialFactory implements EntityFactory<Entity> {

    public static final String WATER = "Water";

    private final ObjectMap<String, EntityPool<Entity>> pools;

    public SpecialFactory(MegamanGame game) {
        this.pools = new ObjectMap<>() {{
            put(WATER, new EntityPool<>(5, () -> new Water(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
