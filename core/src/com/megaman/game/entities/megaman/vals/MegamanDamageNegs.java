package com.megaman.game.entities.megaman.vals;

import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.enemies.impl.*;
import com.megaman.game.entities.hazards.impl.LaserBeamer;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.entities.projectiles.impl.Fireball;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MegamanDamageNegs {

    public static Map<Class<? extends Damager>, DamageNegotiation> get() {
        return dmgNegs;
    }

    public static Set<Class<? extends Damager>> getDamagerMaskSet() {
        return get().keySet();
    }

    public static DamageNegotiation get(Damager damager) {
        return get().get(damager.getClass());
    }

    private static final Map<Class<? extends Damager>, DamageNegotiation> dmgNegs = new HashMap<>() {{
        put(Bat.class, new DamageNegotiation(2));
        put(Met.class, new DamageNegotiation(2));
        put(MagFly.class, new DamageNegotiation(2));
        put(FlyBoy.class, new DamageNegotiation(4));
        put(Bullet.class, new DamageNegotiation(2));
        put(ChargedShot.class, new DamageNegotiation(4));
        put(Fireball.class, new DamageNegotiation(2));
        put(Dragonfly.class, new DamageNegotiation(3));
        put(Matasaburo.class, new DamageNegotiation(2));
        put(SniperJoe.class, new DamageNegotiation(3));
        put(SpringHead.class, new DamageNegotiation(2));
        put(FloatingCan.class, new DamageNegotiation(2));
        put(LaserBeamer.class, new DamageNegotiation(3));
        put(SuctionRoller.class, new DamageNegotiation(1));
        put(GapingFish.class, new DamageNegotiation(2));
    }};

}
