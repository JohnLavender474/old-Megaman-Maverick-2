package com.megaman.game.entities;

import com.megaman.game.MegamanGame;
import com.megaman.game.entities.blocks.BlockFactory;
import com.megaman.game.entities.explosions.ExplosionFactory;
import com.megaman.game.entities.projectiles.ProjectileFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class EntityFactories {

    private final Map<EntityType, EntityFactory<? extends Entity>> factories;

    public EntityFactories(MegamanGame game) {
        factories = new EnumMap<>(EntityType.class) {{
            put(EntityType.BLOCK, new BlockFactory(game));
            put(EntityType.EXPLOSION, new ExplosionFactory(game));
            put(EntityType.PROJECTILE, new ProjectileFactory(game));
        }};
    }

    public Entity fetch(EntityType entityType, String key) {
        if (entityType == EntityType.MEGAMAN) {
            throw new IllegalStateException("Megaman should not be fetched via a factory");
        }
        return factories.get(entityType).fetch(key);
    }

}
