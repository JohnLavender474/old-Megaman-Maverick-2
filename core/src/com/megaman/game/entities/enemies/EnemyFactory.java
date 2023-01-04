package com.megaman.game.entities.enemies;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityFactory;
import com.megaman.game.entities.EntityPool;
import com.megaman.game.entities.enemies.impl.*;

public class EnemyFactory implements EntityFactory {

    public static final String MET = "Met";
    public static final String BAT = "Bat";
    public static final String MAG_FLY = "MagFly";
    public static final String FLY_BOY = "FlyBoy";
    public static final String PENGUIN = "Penguin";
    public static final String SNIPER_JOE = "SniperJoe";
    public static final String DRAGON_FLY = "Dragonfly";
    public static final String MATASABURO = "Matasaburo";
    public static final String SPRING_HEAD = "SpringHead";
    public static final String SWINGIN_JOE = "SwinginJoe";
    public static final String GAPING_FISH = "GapingFish";
    public static final String FLOATING_CAN = "FloatingCan";
    public static final String SUCTION_ROLLER = "SuctionRoller";
    public static final String SHIELD_ATTACKER = "ShieldAttacker";

    private final ObjectMap<String, EntityPool> pools;

    public EnemyFactory(MegamanGame game) {
        pools = new ObjectMap<>() {{
            put(MET, new EntityPool(2, () -> new Met(game)));
            put(BAT, new EntityPool(5, () -> new Bat(game)));
            put(MAG_FLY, new EntityPool(3, () -> new MagFly(game)));
            put(FLY_BOY, new EntityPool(3, () -> new FlyBoy(game)));
            put(PENGUIN, new EntityPool(2, () -> new Penguin(game)));
            put(SNIPER_JOE, new EntityPool(3, () -> new SniperJoe(game)));
            put(DRAGON_FLY, new EntityPool(3, () -> new Dragonfly(game)));
            put(MATASABURO, new EntityPool(3, () -> new Matasaburo(game)));
            put(SPRING_HEAD, new EntityPool(3, () -> new SpringHead(game)));
            put(SWINGIN_JOE, new EntityPool(3, () -> new SwinginJoe(game)));
            put(GAPING_FISH, new EntityPool(3, () -> new GapingFish(game)));
            put(FLOATING_CAN, new EntityPool(5, () -> new FloatingCan(game)));
            put(SUCTION_ROLLER, new EntityPool(3, () -> new SuctionRoller(game)));
            put(SHIELD_ATTACKER, new EntityPool(3, () -> new ShieldAttacker(game)));
        }};
    }

    @Override
    public Entity fetch(String key) {
        return pools.get(key).fetch();
    }

}
