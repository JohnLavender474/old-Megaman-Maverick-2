package com.megaman.game.entities.explosions;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;
import com.megaman.game.entities.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.explosions.impl.Disintegration;

public class ExplosionFactory implements EntityFactory<Entity> {

    public static final String DISINTEGRATION = "Disintegration";
    public static final String CHARGED_SHOT_EXPLOSION = "ChargedShotExplosion";

    private final ObjectMap<String, EntityPool<Entity>> pools;

    public ExplosionFactory(MegamanGame game) {
        pools = new ObjectMap<>() {{
            put(DISINTEGRATION, new EntityPool<>(10, () -> new Disintegration(game)));
            put(CHARGED_SHOT_EXPLOSION, new EntityPool<>(5, () -> new ChargedShotExplosion(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
