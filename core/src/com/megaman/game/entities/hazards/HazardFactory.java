package com.megaman.game.entities.hazards;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;
import com.megaman.game.entities.hazards.impl.LaserBeamer;
import com.megaman.game.entities.hazards.impl.Saw;

public class HazardFactory implements EntityFactory {

    public static final String SAW = "Saw";
    public static final String LASER_BEAMER = "LaserBeamer";

    private final ObjectMap<String, EntityPool> pools;

    public HazardFactory(MegamanGame game) {
        pools = new ObjectMap<>() {{
            put(SAW, new EntityPool(5, () -> new Saw(game)));
            put(LASER_BEAMER, new EntityPool(5, () -> new LaserBeamer(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
