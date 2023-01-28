package com.megaman.game.entities.impl.enemies.impl;

import com.megaman.game.MegamanGame;
import com.megaman.game.entities.utils.damage.DamageNegotiation;
import com.megaman.game.entities.utils.damage.Damager;
import com.megaman.game.entities.utils.faceable.Faceable;
import com.megaman.game.entities.utils.faceable.Facing;
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
        defineBody();
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDmgNegs() {
        return new HashMap<>() {{

        }};
    }

    protected void defineBody() {

    }



}
