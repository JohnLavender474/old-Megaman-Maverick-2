package com.megaman.game.entities.impl.explosions;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.utils.factories.EntityFactory;
import com.megaman.game.entities.utils.factories.EntityPool;
import com.megaman.game.entities.impl.explosions.impl.*;

public class ExplosionFactory implements EntityFactory {

    public static final String EXPLOSION = "Explosion";
    public static final String EXPLOSION_ORB = "ExplosionOrb";
    public static final String DISINTEGRATION = "Disintegration";
    public static final String SNOWBALL_EXPLOSION = "SnowballExplosion";
    public static final String PRECIOUS_EXPLOSION = "PreciousExplosion";
    public static final String CHARGED_SHOT_EXPLOSION = "ChargedShotExplosion";

    private final ObjectMap<String, EntityPool> pools;

    public ExplosionFactory(MegamanGame game) {
        pools = new ObjectMap<>() {{
            put(EXPLOSION, new EntityPool(5, () -> new Explosion(game)));
            put(EXPLOSION_ORB, new EntityPool(8, () -> new ExplosionOrb(game)));
            put(DISINTEGRATION, new EntityPool(10, () -> new Disintegration(game)));
            put(SNOWBALL_EXPLOSION, new EntityPool(3, () -> new SnowballExplosion(game)));
            put(PRECIOUS_EXPLOSION, new EntityPool(10, () -> new PreciousExplosion(game)));
            put(CHARGED_SHOT_EXPLOSION, new EntityPool(3, () -> new ChargedShotExplosion(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
