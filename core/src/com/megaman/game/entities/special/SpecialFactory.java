package com.megaman.game.entities.special;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;
import com.megaman.game.entities.special.impl.SpringBouncer;
import com.megaman.game.entities.special.impl.Water;

public class SpecialFactory implements EntityFactory {

    public static final String WATER = "Water";
    public static final String SPRING_BOUNCER = "SpringBouncer";

    private final ObjectMap<String, EntityPool> pools;

    public SpecialFactory(MegamanGame game) {
        this.pools = new ObjectMap<>() {{
            put(WATER, new EntityPool(5, () -> new Water(game)));
            put(SPRING_BOUNCER, new EntityPool(5, () -> new SpringBouncer(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
