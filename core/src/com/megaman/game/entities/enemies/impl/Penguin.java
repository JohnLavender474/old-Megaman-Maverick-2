package com.megaman.game.entities.enemies.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Faceable;
import com.megaman.game.entities.Facing;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.entities.projectiles.impl.Fireball;
import com.megaman.game.health.HealthVals;
import com.megaman.game.shapes.ShapeHandle;
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

public class Penguin extends Enemy implements Faceable {

    private static final float STAND_DUR = 1f;
    private static final float SLIDE_DUR = .35f;
    private static final float G_GRAV = -.0015f;
    private static final float GRAV = -.375f;
    private static final float JUMP_X = 8f;
    private static final float JUMP_Y = 28f;
    private static final float SLIDE_X = 11f;

    private final Sprite sprite;
    private final Timer standTimer;
    private final Timer slideTimer;

    @Getter
    @Setter
    private Facing facing;

    public Penguin(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        sprite = new Sprite();
        standTimer = new Timer(STAND_DUR);
        slideTimer = new Timer(SLIDE_DUR);
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
        slideTimer.setToEnd();
        standTimer.reset();
    }

    public boolean isSliding() {
        return !slideTimer.isFinished() && is(BodySense.FEET_ON_GROUND);
    }

    public boolean isJumping() {
        return !slideTimer.isFinished() && !is(BodySense.FEET_ON_GROUND);
    }

    public boolean isStanding() {
        return slideTimer.isFinished();
    }

    private void jump() {
        standTimer.setToEnd();
        slideTimer.reset();
        Vector2 impulse = new Vector2();
        impulse.x = JUMP_X * WorldVals.PPM;
        if (is(Facing.LEFT)) {
            impulse.x *= -1f;
        }
        impulse.y = JUMP_Y * WorldVals.PPM;
        body.velocity.add(impulse);
    }

    private void stand(float delta) {
        setFacing(game.getMegaman().body.isRightOf(body) ? Facing.RIGHT : Facing.LEFT);
        standTimer.update(delta);
        if (is(BodySense.FEET_ON_GROUND) && standTimer.isFinished()) {
            jump();
        }
    }

    private void slide(float delta) {
        slideTimer.update(delta);
        if (slideTimer.isFinished()) {
            standTimer.reset();
        }
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(5));
            put(Fireball.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShot.class, new DamageNegotiation(damager ->
                    ((ChargedShot) damager).isFullyCharged() ? 25 : 10));
            put(ChargedShotExplosion.class, new DamageNegotiation(damager ->
                    ((ChargedShotExplosion) damager).isFullyCharged() ? 15 : 10));
        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.gravityOn = true;
        body.affectedByResistance = true;
        Array<ShapeHandle> h = new Array<>();

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY, new Rectangle());
        body.add(bodyFixture);

        // feet fixture
        Fixture feetFixture = new Fixture(this, FixtureType.FEET,
                new Rectangle().setHeight(.1f * WorldVals.PPM));
        h.add(new ShapeHandle(feetFixture.shape, Color.GREEN));
        body.add(feetFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Rectangle());
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Rectangle());
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);

        body.preProcess = delta -> {
            Rectangle feetBounds = (Rectangle) feetFixture.shape;
            if (isStanding() || isJumping()) {
                body.bounds.setSize(.75f * WorldVals.PPM, WorldVals.PPM);
                feetBounds.width = .65f * WorldVals.PPM;
                feetFixture.offset.y = -.5f * WorldVals.PPM;
            } else {
                body.bounds.setSize(WorldVals.PPM, .5f * WorldVals.PPM);
                feetBounds.width = .9f * WorldVals.PPM;
                feetFixture.offset.y = -.25f * WorldVals.PPM;
            }
            ((Rectangle) damageableFixture.shape).set(body.bounds);
            ((Rectangle) damagerFixture.shape).set(body.bounds);
            body.gravity.y = (is(BodySense.FEET_ON_GROUND) ? G_GRAV : GRAV) * WorldVals.PPM;
            if (isSliding()) {
                body.velocity.x = SLIDE_X * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    body.velocity.x *= -1f;
                }
            }
        };
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            if (isStanding()) {
                stand(delta);
            } else if (isSliding()) {
                slide(delta);
            }
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
            h.hidden = dmgBlink;
            if (isSliding()) {
                sprite.translateY(-.25f * WorldVals.PPM);
            }
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> {
            if (isStanding()) {
                return Math.abs(body.velocity.x) > WorldVals.PPM / 4f ? "Slippin" : "Stand";
            }
            return isJumping() ? "Jump" : "Slide";
        };
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Stand", new Animation(atlas.findRegion("Penguin/Stand"), 2, .1f));
            put("Jump", new Animation(atlas.findRegion("Penguin/Jump")));
            put("Slide", new Animation(atlas.findRegion("Penguin/Slide")));
            put("Slippin", new Animation(atlas.findRegion("Penguin/Slippin")));
        }});
    }

}
