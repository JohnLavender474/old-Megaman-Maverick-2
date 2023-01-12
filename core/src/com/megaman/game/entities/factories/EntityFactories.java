package com.megaman.game.entities.factories;

import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.impl.blocks.BlockFactory;
import com.megaman.game.entities.impl.decorations.DecorationFactory;
import com.megaman.game.entities.impl.enemies.EnemyFactory;
import com.megaman.game.entities.impl.explosions.ExplosionFactory;
import com.megaman.game.entities.impl.hazards.HazardFactory;
import com.megaman.game.entities.impl.items.ItemFactory;
import com.megaman.game.entities.impl.projectiles.ProjectileFactory;
import com.megaman.game.entities.impl.sensors.SensorFactory;
import com.megaman.game.entities.impl.special.SpecialFactory;

import java.util.EnumMap;
import java.util.Map;

public class EntityFactories {

    private final Map<EntityType, EntityFactory> factories;

    public EntityFactories(MegamanGame game) {
        factories = new EnumMap<>(EntityType.class) {{
            put(EntityType.ITEM, new ItemFactory(game));
            put(EntityType.ENEMY, new EnemyFactory(game));
            put(EntityType.BLOCK, new BlockFactory(game));
            put(EntityType.SENSOR, new SensorFactory(game));
            put(EntityType.HAZARD, new HazardFactory(game));
            put(EntityType.SPECIAL, new SpecialFactory(game));
            put(EntityType.EXPLOSION, new ExplosionFactory(game));
            put(EntityType.PROJECTILE, new ProjectileFactory(game));
            put(EntityType.DECORATION, new DecorationFactory(game));
        }};
    }

    public Entity fetch(EntityType entityType, String key) {
        if (entityType == EntityType.MEGAMAN) {
            throw new IllegalStateException("Megaman should not be fetched via factory");
        }
        return factories.get(entityType).fetch(key);
    }

}
