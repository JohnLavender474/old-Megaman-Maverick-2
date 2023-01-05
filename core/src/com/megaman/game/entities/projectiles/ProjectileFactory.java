package com.megaman.game.entities.projectiles;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;
import com.megaman.game.entities.projectiles.impl.*;

public class ProjectileFactory implements EntityFactory {

    public static final String BULLET = "Bullet";
    public static final String JOEBALL = "Joeball";
    public static final String PICKET = "Picket";
    public static final String FIREBALL = "Fireball";
    public static final String SNOWBALL = "Snowball";
    public static final String CHARGED_SHOT = "ChargedShot";

    private final ObjectMap<String, EntityPool> pools;

    public ProjectileFactory(MegamanGame game) {
        this.pools = new ObjectMap<>() {{
            put(BULLET, new EntityPool(20, () -> new Bullet(game)));
            put(JOEBALL, new EntityPool(3, () -> new JoeBall(game)));
            put(PICKET, new EntityPool(10, () -> new Picket(game)));
            put(FIREBALL, new EntityPool(3, () -> new Fireball(game)));
            put(SNOWBALL, new EntityPool(3, () -> new Snowball(game)));
            put(CHARGED_SHOT, new EntityPool(2, () -> new ChargedShot(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
