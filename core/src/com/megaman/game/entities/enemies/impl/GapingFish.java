package com.megaman.game.entities.enemies.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.animations.Animator;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.*;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GapingFish extends Enemy implements Faceable {

    private static final float HORIZ_SPEED = 2f;
    private static final float VERT_SPEED = 1.25f;
    private static final float CHOMP_DUR = 1.25f;

    private final Sprite sprite;
    private final Timer chompTimer;

    @Getter
    @Setter
    private Facing facing;

    public GapingFish(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        sprite = new Sprite();
        chompTimer = new Timer(CHOMP_DUR, true);
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getCenterPoint(bounds);
        body.bounds.setCenter(spawn);
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    @Override
    public void onDamageInflictedTo(Damageable damageable) {
        if (damageable instanceof Megaman) {
            chompTimer.reset();
        }
    }

    public boolean isChomping() {
        return !chompTimer.isFinished();
    }

    @Override
    protected void defineBody(Body body) {
        body.bounds.setSize(WorldVals.PPM);
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY, new Rectangle().setSize(WorldVals.PPM));
        body.fixtures.add(bodyFixture);
        Rectangle m1 = new Rectangle().setSize(.75f * WorldVals.PPM, .2f * WorldVals.PPM);
        Fixture headFixture = new Fixture(this, FixtureType.HEAD, new Rectangle(m1));
        headFixture.offset.y = .375f * WorldVals.PPM;
        body.fixtures.add(headFixture);
        Fixture feetFixture = new Fixture(this, FixtureType.FEET, new Rectangle(m1));
        feetFixture.offset.y = -.375f * WorldVals.PPM;
        body.fixtures.add(feetFixture);
        Rectangle m2 = new Rectangle().setSize(.75f * WorldVals.PPM, WorldVals.PPM);
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Rectangle(m2));
        body.fixtures.add(damageableFixture);
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Rectangle(m2));
        body.fixtures.add(damagerFixture);
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            Body megaBody = game.getMegaman().getComponent(BodyComponent.class).body;
            if (body.bounds.x >= megaBody.bounds.x + megaBody.bounds.width) {
                setFacing(Facing.LEFT);
            } else if (body.bounds.x + body.bounds.width <= megaBody.bounds.x) {
                setFacing(Facing.RIGHT);
            }
            if (isChomping() || !dmgTimer.isFinished()) {
                body.velocity.setZero();
            } else {
                Vector2 vel = body.velocity;
                vel.x = HORIZ_SPEED * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    vel.x *= -1f;
                }
                if (body.is(BodySense.IN_WATER)) {
                    vel.y = VERT_SPEED;
                    if (megaBody.bounds.y <= body.bounds.y + body.bounds.height) {
                        vel.y *= -1f;
                    }
                    vel.y *= WorldVals.PPM;
                } else {
                    vel.y = 0f;
                }
            }
            chompTimer.update(delta);
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> {
            if (isChomping()) {
                return "Chomping";
            }
            return isInvincible() ? "Gaping" : "Swimming";
        };
        TextureAtlas textureAtlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(new Animator(sprite, keySupplier, new ObjectMap<>() {{
            put("Chomping", new Animation(textureAtlas.findRegion("GapingFish/Chomping"), 2, .1f));
            put("Gaping", new Animation(textureAtlas.findRegion("GapingFish/Gaping"), 2, .15f));
            put("Swimming", new Animation(textureAtlas.findRegion("GapingFish/Swimming"), 2, .15f));
        }}));
    }

}