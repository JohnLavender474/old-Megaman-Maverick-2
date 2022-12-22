package com.megaman.game.entities.decorations;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;

public class DecorationFactory implements EntityFactory<Entity> {

    public static final String SPLASH = "Splash";
    public static final String SMOKE_PUFF = "SmokePuff";

    private final ObjectMap<String, EntityPool<Entity>> pools;

    public DecorationFactory(MegamanGame game) {
        this.pools = new ObjectMap<>() {{
            put(SPLASH, new EntityPool<>(5, () -> new Splash(game)));
            put(SMOKE_PUFF, new EntityPool<>(5, () -> new SmokePuff(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
