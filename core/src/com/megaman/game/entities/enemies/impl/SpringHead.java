package com.megaman.game.entities.enemies.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.ViewVals;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Faceable;
import com.megaman.game.entities.Facing;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.shapes.ShapeComponent;
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
import java.util.function.Function;
import java.util.function.Supplier;

public class SpringHead extends Enemy implements Faceable {


    private static final float SPEED_NORMAL = 3.5f;
    private static final float SPEED_SUPER = 8f;

    private static final float BOUNCE_DUR = 1.5f;

    private static final float DAMAGE_DUR = .5f;
    private static final float TURN_DELAY = .25f;

    private static final float X_BOUNCE = 10f;
    private static final float Y_BOUNCE = 20f;

    private final Sprite sprite;
    private final Timer turnTimer;
    private final Timer bounceTimer;
    private final Rectangle speedUpScanner;

    @Getter
    @Setter
    private Facing facing;

    public SpringHead(MegamanGame game) {
        super(game, DAMAGE_DUR, BodyType.DYNAMIC);
        sprite = new Sprite();
        turnTimer = new Timer(TURN_DELAY);
        bounceTimer = new Timer(BOUNCE_DUR, true);
        speedUpScanner = new Rectangle().setSize(ViewVals.VIEW_WIDTH * WorldVals.PPM, WorldVals.PPM / 4f);
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
            put(ChargedShot.class, new DamageNegotiation(10));
            put(ChargedShotExplosion.class, new DamageNegotiation(5));
        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.bounds.setSize(WorldVals.PPM / 4f, WorldVals.PPM / 4f);
        Array<ShapeHandle> h = new Array<>();
        Circle c1 = new Circle();
        c1.radius = .35f * WorldVals.PPM;

        // left fixture
        Fixture leftFixture = new Fixture(this, FixtureType.SIDE,
                new Rectangle().setSize(.1f * WorldVals.PPM));
        leftFixture.putUserData(ConstKeys.SIDE, ConstKeys.LEFT);
        leftFixture.offset.x = -.4f * WorldVals.PPM;
        leftFixture.offset.y = -WorldVals.PPM / 4f;
        h.add(new ShapeHandle(leftFixture.shape, Color.PURPLE));
        body.add(leftFixture);

        // right fixture
        Fixture rightFixture = new Fixture(this, FixtureType.SIDE,
                new Rectangle().setSize(.1f * WorldVals.PPM));
        rightFixture.putUserData(ConstKeys.SIDE, ConstKeys.RIGHT);
        rightFixture.offset.x = .4f * WorldVals.PPM;
        rightFixture.offset.y = -WorldVals.PPM / 4f;
        h.add(new ShapeHandle(rightFixture.shape, Color.PURPLE));
        body.add(rightFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Circle(c1));
        h.add(new ShapeHandle(damagerFixture.shape));
        body.add(damagerFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Circle(c1));
        body.add(damageableFixture);

        // shield fixture
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(.85f * WorldVals.PPM, .6f * WorldVals.PPM));
        shieldFixture.offset.y = .1f * WorldVals.PPM;
        shieldFixture.putUserData(ConstKeys.REFLECT, ConstKeys.UP);
        h.add(new ShapeHandle(shieldFixture.shape, Color.BLUE));
        body.add(shieldFixture);

        Circle c2 = new Circle();
        c2.radius = WorldVals.PPM / 4f;

        // bounce fixture
        Fixture bounceFixture = new Fixture(this, FixtureType.BOUNCER, c2);
        bounceFixture.putUserData(ConstKeys.FUNCTION, (Function<Fixture, Vector2>) f -> {
            bounceTimer.reset();
            Vector2 force = new Vector2();
            force.x = X_BOUNCE * WorldVals.PPM;
            Vector2 c = ShapeUtils.getCenter(f.shape);
            if (c.x < body.getCenter().x) {
                force.x *= -1f;
            }
            force.y = Y_BOUNCE * WorldVals.PPM;
            return force;
        });
        h.add(new ShapeHandle(bounceFixture.shape, Color.GREEN));
        body.add(bounceFixture);
        putComponent(new ShapeComponent(h));
    }

    public boolean isBouncing() {
        return !bounceTimer.isFinished();
    }

    private boolean isMegamanRight() {
        return game.getMegaman().body.isRightOf(body);
    }

    private boolean isMegamanOverlappingSpeedUpScanner() {
        return game.getMegaman().body.overlaps(speedUpScanner);
    }

    private boolean isFacingWrongDir() {
        return ((isMegamanRight() && is(Facing.LEFT)) || (!isMegamanRight() && is(Facing.RIGHT)));
    }

    // TODO: glitch, spring head gets stuck due to isAtLedge()
    /*
    private boolean isAtLedge() {
        return (is(Facing.LEFT) && !is(BodySense.SIDE_TOUCHING_BLOCK_LEFT)) ||
                (is(Facing.RIGHT) && !is(BodySense.SIDE_TOUCHING_BLOCK_RIGHT));
    }
     */

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            turnTimer.update(delta);
            if (turnTimer.isJustFinished()) {
                setFacing(isMegamanRight() ? Facing.RIGHT : Facing.LEFT);
            }
            if (turnTimer.isFinished() && isFacingWrongDir()) {
                turnTimer.reset();
            }
            bounceTimer.update(delta);

            // TODO: glitch, spring head gets stuck due to isAtLedge()
            /*
            if (isBouncing() || isAtLedge()) {
                body.velocity.x = 0f;
                return;
            }
             */
            if (isBouncing()) {
                body.velocity.x = 0f;
                return;
            }

            float vel = (isMegamanOverlappingSpeedUpScanner() ? SPEED_SUPER : SPEED_NORMAL) * WorldVals.PPM;
            body.velocity.x = is(Facing.LEFT) ? -vel : vel;
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 3);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.SPRING_HEAD);
        Supplier<String> keySupplier = () -> isBouncing() ? "Unleashed" : "Compressed";
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Unleashed", new Animation(atlas.findRegion("Unleashed"), 6, .1f));
            put("Compressed", new Animation(atlas.findRegion("Compressed")));
        }});
    }

}
