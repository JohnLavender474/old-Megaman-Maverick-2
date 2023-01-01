package com.megaman.game.entities.enemies.impl;

import com.megaman.game.MegamanGame;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Faceable;
import com.megaman.game.entities.Facing;
import com.megaman.game.entities.enemies.Enemy;
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
