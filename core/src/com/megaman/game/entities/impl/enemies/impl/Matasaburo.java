package com.megaman.game.entities.impl.enemies.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.damage.DamageNegotiation;
import com.megaman.game.entities.damage.Damager;
import com.megaman.game.entities.faceable.Faceable;
import com.megaman.game.entities.faceable.Facing;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.impl.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.impl.projectiles.Projectile;
import com.megaman.game.entities.impl.projectiles.impl.Bullet;
import com.megaman.game.entities.impl.projectiles.impl.ChargedShot;
import com.megaman.game.entities.impl.projectiles.impl.Fireball;
import com.megaman.game.health.HealthVals;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.UpdateFunc;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class Matasaburo extends Enemy implements Faceable {

    private static final float DAMAGE_DUR = .35f;
    private static final float BLOW_FORCE = 25f;

    private static TextureRegion matasaburoReg;

    private final Sprite sprite;

    @Getter
    @Setter
    private Facing facing;

    public Matasaburo(MegamanGame game) {
        super(game, DAMAGE_DUR, BodyType.DYNAMIC);
        if (matasaburoReg == null) {
            matasaburoReg = game.getAssMan().getTextureRegion(TextureAsset.ENEMIES_1, "Matasaburo");
        }
        sprite = new Sprite();
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(10));
            put(Fireball.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShot.class, new DamageNegotiation(damager ->
                    ((ChargedShot) damager).isFullyCharged() ? HealthVals.MAX_HEALTH : 10));
            put(ChargedShotExplosion.class, new DamageNegotiation(damager ->
                    ((ChargedShotExplosion) damager).isFullyCharged() ? 15 : 5));
        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.bounds.setSize(WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // blow fixture
        Fixture blowFixture = new Fixture(this, FixtureType.FORCE,
                new Rectangle().setSize(10f * WorldVals.PPM, WorldVals.PPM * 1.15f));

        // TODO: test

        /*
        Function<Fixture, Vector2> blowFunc = f -> {
            if (f.entity instanceof Enemy) {
                return Vector2.Zero;
            }
            if (f.entity instanceof Projectile p) {
                p.owner = null;
            }
            float force = BLOW_FORCE * WorldVals.PPM;
            if (is(Facing.LEFT)) {
                force *= -1f;
            }
            return new Vector2(force, 0f);
        };
         */

        /*
        Function<Float, Vector2> blowFunc = delta -> {
            float force = BLOW_FORCE * WorldVals.PPM * delta;
            if (is(Facing.LEFT)) {
                force *= -1f;
            }
            return new Vector2(force, 0f);
        };
         */

        UpdateFunc<Fixture, Vector2> blowFunc = (f, delta) -> {
            if (f.entity instanceof Enemy) {
                return Vector2.Zero;
            }
            if (f.entity instanceof Projectile p) {
                p.owner = null;
            }
            float force = BLOW_FORCE * WorldVals.PPM;
            if (is(Facing.LEFT)) {
                force *= -1f;
            }
            return new Vector2(force * delta, 0f);
        };

        blowFixture.putUserData(ConstKeys.FUNCTION, blowFunc);

        h.add(new ShapeHandle(blowFixture.shape, Color.BLUE));
        body.add(blowFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.85f * WorldVals.PPM));
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(WorldVals.PPM));
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);

        // pre-process
        body.preProcess = delta -> {
            float offsetX = 5f * WorldVals.PPM;
            if (is(Facing.LEFT)) {
                offsetX *= -1f;
            }
            blowFixture.offset.x = offsetX;
        };

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> setFacing(game.getMegaman().body.isRightOf(body) ? Facing.RIGHT : Facing.LEFT));
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 3);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(matasaburoReg, 6, .1f));
    }

}
