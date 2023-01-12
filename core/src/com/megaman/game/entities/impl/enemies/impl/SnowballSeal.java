package com.megaman.game.entities.impl.enemies.impl;

import com.megaman.game.MegamanGame;
import com.megaman.game.entities.damage.DamageNegotiation;
import com.megaman.game.entities.damage.Damager;
import com.megaman.game.entities.faceable.Faceable;
import com.megaman.game.entities.faceable.Facing;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.world.Body;
import com.megaman.game.world.BodyType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class SnowballSeal extends Enemy implements Faceable {

    private static final float WAIT_DUR = 1f;
    private static final float CHARGE_SNOWBALL_DUR = .5f;

    @Getter
    @Setter
    private Facing facing;

    public SnowballSeal(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    @Override
    protected void defineBody(Body body) {

    }



}
