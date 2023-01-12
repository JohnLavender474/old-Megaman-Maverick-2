package com.megaman.game.entities.impl.bosses.impl;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.entities.damage.DamageNegotiation;
import com.megaman.game.entities.damage.Damager;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Body;
import com.megaman.game.world.BodyType;

import java.util.HashMap;
import java.util.Map;

public class GutsTank extends Enemy {

    // size = 10w x 8h
    // move 10 tiles in 8 secs

    public GutsTank(MegamanGame game) {
        super(game, BodyType.ABSTRACT);
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {

    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    @Override
    protected void defineBody(Body body) {

    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);

    }

    private SpriteComponent spriteComponent() {
        return null;
    }

    private AnimationComponent animationComponent() {
        return null;
    }

}
