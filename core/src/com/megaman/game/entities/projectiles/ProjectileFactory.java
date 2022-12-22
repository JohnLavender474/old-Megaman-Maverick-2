package com.megaman.game.entities.projectiles;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.entities.projectiles.impl.Fireball;

public class ProjectileFactory implements EntityFactory {

    public static final String BULLET = "Bullet";
    public static final String FIREBALL = "Fireball";
    public static final String CHARGED_SHOT = "ChargedShot";

    private final ObjectMap<String, EntityPool> pools;

    public ProjectileFactory(MegamanGame game) {
        this.pools = new ObjectMap<>() {{
            put(BULLET, new EntityPool(100, () -> new Bullet(game)));
            put(FIREBALL, new EntityPool(5, () -> new Fireball(game)));
            put(CHARGED_SHOT, new EntityPool(5, () -> new ChargedShot(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
