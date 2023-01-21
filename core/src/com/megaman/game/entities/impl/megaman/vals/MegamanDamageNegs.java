package com.megaman.game.entities.impl.megaman.vals;

import com.megaman.game.entities.utils.damage.DamageNegotiation;
import com.megaman.game.entities.utils.damage.Damager;
import com.megaman.game.entities.impl.enemies.impl.*;
import com.megaman.game.entities.impl.explosions.impl.PreciousExplosion;
import com.megaman.game.entities.impl.hazards.impl.LaserBeamer;
import com.megaman.game.entities.impl.projectiles.impl.*;

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
        put(Screwie.class, new DamageNegotiation(1));
        put(Bullet.class, new DamageNegotiation(2));
        put(Picket.class, new DamageNegotiation(2));
        put(Ratton.class, new DamageNegotiation(2));
        put(MagFly.class, new DamageNegotiation(2));
        put(FlyBoy.class, new DamageNegotiation(4));
        put(Penguin.class, new DamageNegotiation(3));
        put(Snowball.class, new DamageNegotiation(1));
        put(ChargedShot.class, new DamageNegotiation(4));
        put(Fireball.class, new DamageNegotiation(2));
        put(PreciousShot.class, new DamageNegotiation(2));
        put(PreciousExplosion.class, new DamageNegotiation(1));
        put(Dragonfly.class, new DamageNegotiation(3));
        put(Matasaburo.class, new DamageNegotiation(2));
        put(SniperJoe.class, new DamageNegotiation(3));
        put(SpringHead.class, new DamageNegotiation(2));
        put(FloatingCan.class, new DamageNegotiation(2));
        put(LaserBeamer.class, new DamageNegotiation(3));
        put(SuctionRoller.class, new DamageNegotiation(2));
        put(ShieldAttacker.class, new DamageNegotiation(2));
        put(GapingFish.class, new DamageNegotiation(2));
    }};

}
