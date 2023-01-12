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
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class SuctionRoller extends Enemy implements Faceable {

    private static final float GRAVITY = -.15f;
    private static final float VEL_X = 2.5f;
    private static final float VEL_Y = 2.5f;

    private static TextureRegion sucRollReg;

    private final Sprite sprite;

    private boolean onWall;
    private boolean wasOnWall;
    @Getter
    @Setter
    private Facing facing;

    public SuctionRoller(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        if (sucRollReg == null) {
            sucRollReg = game.getAssMan().getTextureRegion(TextureAsset.ENEMIES_1, "SuctionRoller");
        }
        sprite = new Sprite();
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        onWall = wasOnWall = false;
        facing = game.getMegaman().body.bounds.x > body.bounds.x ? Facing.RIGHT : Facing.LEFT;
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(5));
            put(Fireball.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShot.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShotExplosion.class, new DamageNegotiation(15));
        }};
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            if (game.getMegaman().dead) {
                return;
            }
            wasOnWall = onWall;
            onWall = (is(Facing.LEFT) && body.is(BodySense.SIDE_TOUCHING_BLOCK_LEFT)) ||
                    (is(Facing.RIGHT) && body.is(BodySense.SIDE_TOUCHING_BLOCK_RIGHT));
            if (body.is(BodySense.FEET_ON_GROUND)) {
                if (ShapeUtils.getBottomRightPoint(game.getMegaman().body.bounds).x < body.bounds.x) {
                    setFacing(Facing.LEFT);
                } else if (game.getMegaman().body.bounds.x > ShapeUtils.getBottomRightPoint(body.bounds).x) {
                    setFacing(Facing.RIGHT);
                }
            }
        });
    }

    @Override
    protected void defineBody(Body body) {
        Array<ShapeHandle> h = new Array<>();
        body.gravityOn = true;
        body.bounds.setSize(.75f * WorldVals.PPM, WorldVals.PPM);

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY,
                new Rectangle().setSize(.75f * WorldVals.PPM, WorldVals.PPM));
        h.add(new ShapeHandle(bodyFixture.shape, Color.BLUE));
        body.add(bodyFixture);

        // feet fixture
        Fixture feetFixture = new Fixture(this, FixtureType.FEET,
                new Rectangle().setSize(WorldVals.PPM / 4f, WorldVals.PPM / 32f));
        feetFixture.offset.y = .6f * -WorldVals.PPM;
        h.add(new ShapeHandle(feetFixture.shape, Color.GREEN));
        body.add(feetFixture);

        // left fixture
        Fixture leftFixture = new Fixture(this, FixtureType.SIDE,
                new Rectangle().setSize(WorldVals.PPM / 32f, WorldVals.PPM));
        leftFixture.offset.x = -.375f * WorldVals.PPM;
        leftFixture.offset.y = WorldVals.PPM / 5f;
        leftFixture.putUserData(ConstKeys.SIDE, ConstKeys.LEFT);
        h.add(new ShapeHandle(leftFixture.shape, Color.ORANGE));
        body.add(leftFixture);

        // right fixture
        Fixture rightFixture = new Fixture(this, FixtureType.SIDE,
                new Rectangle().setSize(WorldVals.PPM / 32f, WorldVals.PPM));
        rightFixture.offset.x = .375f * WorldVals.PPM;
        rightFixture.offset.y = WorldVals.PPM / 5f;
        rightFixture.putUserData(ConstKeys.SIDE, ConstKeys.RIGHT);
        h.add(new ShapeHandle(rightFixture.shape, Color.ORANGE));
        body.add(rightFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(.75f * WorldVals.PPM, WorldVals.PPM));
        h.add(new ShapeHandle(damageableFixture.shape, Color.RED));
        body.add(damageableFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.75f * WorldVals.PPM, WorldVals.PPM));
        body.add(damagerFixture);

        // pre-process
        body.preProcess = delta -> {
            body.gravity.y = is(BodySense.FEET_ON_GROUND) ? 0f : GRAVITY * WorldVals.PPM;
            if (onWall) {
                if (!wasOnWall) {
                    body.velocity.x = 0f;
                }
                body.velocity.y = VEL_Y * WorldVals.PPM;
            } else {
                if (wasOnWall) {
                    body.bounds.y += WorldVals.PPM / 10f;
                }
                body.velocity.x = (is(Facing.RIGHT) ? VEL_X : -VEL_X) * WorldVals.PPM;
            }
        };

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        sprite.setOrigin(sprite.getWidth() / 2f, sprite.getHeight() / 2f);
        SpriteHandle h = new SpriteHandle(sprite, 3);
        h.updatable = delta -> {
            h.hidden = dmgBlink;
            sprite.setFlip(is(Facing.RIGHT), false);
            sprite.setRotation(onWall ? (is(Facing.LEFT) ? -90f : 90f) : 0f);
            h.setPosition(body.bounds, onWall ?
                    (is(Facing.LEFT) ? Position.CENTER_LEFT : Position.CENTER_RIGHT) : Position.BOTTOM_CENTER);
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(sucRollReg, 5, .1f));
    }

}
