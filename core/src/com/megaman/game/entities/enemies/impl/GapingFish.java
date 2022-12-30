package com.megaman.game.entities.enemies.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.animations.Animator;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.*;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.entities.projectiles.impl.Fireball;
import com.megaman.game.health.HealthVals;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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
            put(Bullet.class, new DamageNegotiation(10));
            put(Fireball.class, new DamageNegotiation(15));
            put(ChargedShot.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShotExplosion.class, new DamageNegotiation(15));
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
        body.bounds.setSize(WorldVals.PPM, WorldVals.PPM);
        Array<Fixture> scanner = new Array<>();
        Fixture scannerFixture = new Fixture(this, FixtureType.CONSUMER,
                new Rectangle().setSize(WorldVals.PPM, WorldVals.PPM / 2f));
        scannerFixture.putUserData(ConstKeys.CONSUMER, (Consumer<Fixture>) scanner::add);
        scannerFixture.offset.y += WorldVals.PPM / 4f;
        body.add(scannerFixture);
        Rectangle m1 = new Rectangle().setSize(.75f * WorldVals.PPM, .2f * WorldVals.PPM);
        Fixture headFixture = new Fixture(this, FixtureType.HEAD, new Rectangle(m1));
        headFixture.offset.y = .375f * WorldVals.PPM;
        body.add(headFixture);
        Fixture feetFixture = new Fixture(this, FixtureType.FEET, new Rectangle(m1));
        feetFixture.offset.y = -.375f * WorldVals.PPM;
        body.add(feetFixture);
        Rectangle m2 = new Rectangle().setSize(.75f * WorldVals.PPM, WorldVals.PPM);
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Rectangle(m2));
        body.add(damageableFixture);
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Rectangle(m2));
        body.add(damagerFixture);
        body.preProcess = delta -> {
            Body megaBody = game.getMegaman().getComponent(BodyComponent.class).body;
            if (body.bounds.x >= megaBody.bounds.x + megaBody.bounds.width) {
                setFacing(Facing.LEFT);
            } else if (body.bounds.x + body.bounds.width <= megaBody.bounds.x) {
                setFacing(Facing.RIGHT);
            }
            if (isDamaged() || isChomping()) {
                body.velocity.setZero();
            } else {
                Vector2 vel = body.velocity;
                vel.x = HORIZ_SPEED * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    vel.x *= -1f;
                }
                boolean inWater = false;
                for (Fixture f : scanner) {
                    if (f.fixtureType == FixtureType.WATER) {
                        inWater = true;
                        break;
                    }
                }
                scanner.clear();
                if (inWater || !megaBody.isAbove(body)) {
                    vel.y = VERT_SPEED * WorldVals.PPM;
                    if (!megaBody.isAbove(body)) {
                        vel.y *= -1f;
                    }
                } else {
                    vel.y = 0f;
                }
            }
            chompTimer.update(delta);
        };
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
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