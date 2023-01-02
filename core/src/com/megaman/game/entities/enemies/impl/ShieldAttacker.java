package com.megaman.game.entities.enemies.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Faceable;
import com.megaman.game.entities.Facing;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Body;
import com.megaman.game.world.BodyType;
import com.megaman.game.world.WorldVals;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ShieldAttacker extends Enemy implements Faceable {

    private static final float TURN_AROUND_DUR = 1f;
    private static final float X_VEL = 1f;

    private final Sprite sprite;
    private final Timer turnAroundTimer;

    private Vector2 first;
    private Vector2 second;
    @Getter
    @Setter
    private Facing facing;

    public ShieldAttacker(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        sprite = new Sprite();
        turnAroundTimer = new Timer(TURN_AROUND_DUR, true);
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    public boolean isTurningAround() {
        return !turnAroundTimer.isFinished();
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getCenterPoint(bounds);
        body.bounds.setCenter(spawn);
        first = new Vector2(spawn);
        float x = (float) data.get(ConstKeys.X);
        second = new Vector2(spawn).add(x, 0f);
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    @Override
    protected void defineBody(Body body) {

    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            sprite.setFlip(is(Facing.LEFT), false);
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> isTurningAround() ? "TurnAround" : "Attack";
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("TurnAround", new Animation(atlas.findRegion("ShieldAttacker/TurnAround"), 5, .1f, false));
            put("Attack", new Animation(atlas.findRegion("ShieldAttacker/Attack"), 2, .1f));
        }});
    }

}
