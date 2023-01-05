package com.megaman.game.entities.enemies.impl;

import com.badlogic.gdx.graphics.Color;
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
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.entities.projectiles.impl.Fireball;
import com.megaman.game.health.HealthVals;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ShieldAttacker extends Enemy {

    private static final float TURN_AROUND_DUR = .5f;
    private static final float X_VEL = 6f;

    private final Sprite sprite;
    private final Timer turnAroundTimer;

    private float minX;
    private float maxX;
    private boolean left;

    public ShieldAttacker(MegamanGame game) {
        super(game, BodyType.ABSTRACT);
        sprite = new Sprite();
        turnAroundTimer = new Timer(TURN_AROUND_DUR);
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
        float targetX = spawn.x + (float) data.get(ConstKeys.X) * WorldVals.PPM;
        if (spawn.x < targetX) {
            minX = spawn.x;
            maxX = targetX;
            left = false;
        } else {
            minX = targetX;
            maxX = spawn.x;
            left = true;
        }
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(5));
            put(Fireball.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShot.class, new DamageNegotiation(20));
            put(ChargedShotExplosion.class, new DamageNegotiation(5));
        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.bounds.setSize(.75f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Rectangle(body.bounds));
        body.add(damagerFixture);

        // TODO: set damageable fixture behind shield

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setHeight(body.bounds.height));
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);

        // TODO: shield is inactive while turning

        // shield fixture
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(.75f * WorldVals.PPM, 1.25f * WorldVals.PPM));
        shieldFixture.putUserData(ConstKeys.REFLECT, ConstKeys.UP);
        h.add(new ShapeHandle(shieldFixture.shape, Color.GREEN, () -> !isTurningAround()));
        body.add(shieldFixture);

        body.preProcess = delta -> {
            if (isTurningAround()) {
                shieldFixture.active = false;
                damageableFixture.offset.x = 0f;
                ((Rectangle) damageableFixture.shape).width = .5f * WorldVals.PPM;
            } else {
                shieldFixture.active = true;
                damageableFixture.offset.x = (left ? .5f : -.5f) * WorldVals.PPM;
                ((Rectangle) damageableFixture.shape).width = .15f * WorldVals.PPM;
            }
        };

        putComponent(new ShapeComponent(h));
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            float centerX = body.getCenter().x;
            if (centerX < minX || centerX > maxX) {
                turnAroundTimer.reset();
                body.setCenterX(centerX < minX ? minX : maxX);
                body.velocity.setZero();
                left = centerX > maxX;
            }
            turnAroundTimer.update(delta);
            if (turnAroundTimer.isJustFinished()) {
                float x = X_VEL * WorldVals.PPM;
                body.velocity.x = left ? -x : x;
            }
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            sprite.setFlip(isTurningAround() != left, false);
            h.setPosition(body.bounds, Position.CENTER);
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
