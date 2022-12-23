package com.megaman.game.entities.enemies;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;
import com.megaman.game.entities.enemies.impl.Bat;
import com.megaman.game.entities.enemies.impl.Dragonfly;
import com.megaman.game.entities.enemies.impl.FloatingCan;

public class EnemyFactory implements EntityFactory {

    public static final String BAT = "Bat";
    public static final String DRAGON_FLY = "Dragonfly";
    public static final String FLOATING_CAN = "FloatingCan";

    private final ObjectMap<String, EntityPool> pools;

    public EnemyFactory(MegamanGame game) {
        pools = new ObjectMap<>() {{
             put(BAT, new EntityPool(5, () -> new Bat(game)));
             put(DRAGON_FLY, new EntityPool(5, () -> new Dragonfly(game)));
             put(FLOATING_CAN, new EntityPool(10, () -> new FloatingCan(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
