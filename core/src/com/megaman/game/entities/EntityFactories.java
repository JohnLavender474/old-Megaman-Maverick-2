package com.megaman.game.entities;

import com.megaman.game.MegamanGame;
import com.megaman.game.entities.blocks.BlockFactory;

import java.util.EnumMap;
import java.util.Map;

public class EntityFactories {

    private final Map<EntityType, EntityFactory<? extends Entity>> factories = new EnumMap<>(EntityType.class);

    public EntityFactories(MegamanGame game) {
        factories.put(EntityType.BLOCK, new BlockFactory(game));
    }

    public Entity fetch(EntityType entityType, String key) {
        if (entityType == EntityType.MEGAMAN) {
            throw new IllegalStateException("Megaman should not be fetched via a factory");
        }
        return factories.get(entityType).fetch(key);
    }

}
