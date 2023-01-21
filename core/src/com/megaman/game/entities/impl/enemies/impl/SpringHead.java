package com.megaman.game.entities.impl.enemies.impl;

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
import com.megaman.game.entities.utils.bounce.BounceAction;
import com.megaman.game.entities.utils.bounce.BounceDef;
import com.megaman.game.entities.utils.bounce.Bouncer;
import com.megaman.game.entities.utils.damage.DamageNegotiation;
import com.megaman.game.entities.utils.damage.Damager;
import com.megaman.game.entities.utils.faceable.Faceable;
import com.megaman.game.entities.utils.faceable.Facing;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.impl.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.impl.projectiles.impl.ChargedShot;
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
import java.util.function.Supplier;

public class SpringHead extends Enemy implements Faceable, Bouncer {


    private static final float SPEED_NORMAL = 2.5f;
    private static final float SPEED_SUPER = 7f;

    private static final float BOUNCE_DUR = 2f;

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
        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
    }

    @Override
    public BounceDef bounce(Fixture f) {
        if (isBouncing()) {
            return BounceDef.noBounce();
        }
        bounceTimer.reset();
        float x = 0f;
        if (f.entity.hasComponent(BodyComponent.class)) {
            Body bounceable = f.entity.getComponent(BodyComponent.class).body;
            x = (body.isRightOf(bounceable) ? -X_BOUNCE : X_BOUNCE) * WorldVals.PPM;
        }
        return new BounceDef(x, Y_BOUNCE * WorldVals.PPM, BounceAction.ADD, BounceAction.SET);
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{
            put(ChargedShot.class, new DamageNegotiation(10));
            put(ChargedShotExplosion.class, new DamageNegotiation(5));
        }};
    }

    protected void defineBody() {
        body.bounds.setSize(WorldVals.PPM / 4f, WorldVals.PPM / 4f);
        Array<ShapeHandle> h = new Array<>();

        // left fixture
        Fixture leftFixture = new Fixture(this, FixtureType.SIDE,
                new Rectangle().setSize(.1f * WorldVals.PPM));
        leftFixture.putUserData(ConstKeys.SIDE, ConstKeys.LEFT);
        leftFixture.offset.x = -.4f * WorldVals.PPM;
        leftFixture.offset.y = -WorldVals.PPM / 4f;
        h.add(new ShapeHandle(leftFixture.shape, Color.PINK));
        body.add(leftFixture);

        // right fixture
        Fixture rightFixture = new Fixture(this, FixtureType.SIDE,
                new Rectangle().setSize(.1f * WorldVals.PPM));
        rightFixture.putUserData(ConstKeys.SIDE, ConstKeys.RIGHT);
        rightFixture.offset.x = .4f * WorldVals.PPM;
        rightFixture.offset.y = -WorldVals.PPM / 4f;
        h.add(new ShapeHandle(rightFixture.shape, Color.PINK));
        body.add(rightFixture);

        Circle c1 = new Circle();
        c1.radius = .5f * WorldVals.PPM;

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Circle(c1));
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Circle(c1));
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);

        // shield fixture
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(.85f * WorldVals.PPM, .6f * WorldVals.PPM));
        shieldFixture.offset.y = .1f * WorldVals.PPM;
        shieldFixture.putUserData(ConstKeys.REFLECT, ConstKeys.UP);
        h.add(new ShapeHandle(shieldFixture.shape, Color.BLUE));
        body.add(shieldFixture);

        Circle c2 = new Circle();
        c2.radius = .35f * WorldVals.PPM;

        // bounce fixture
        Fixture bounceFixture = new Fixture(this, FixtureType.BOUNCER, c2);
        h.add(new ShapeHandle(bounceFixture.shape, Color.GREEN));
        body.add(bounceFixture);

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }
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

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            speedUpScanner.setCenter(body.getCenter());
            turnTimer.update(delta);
            if (turnTimer.isJustFinished()) {
                setFacing(isMegamanRight() ? Facing.RIGHT : Facing.LEFT);
            }
            if (turnTimer.isFinished() && isFacingWrongDir()) {
                turnTimer.reset();
            }
            bounceTimer.update(delta);
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
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        Supplier<String> keySupplier = () -> isBouncing() ? "Unleashed" : "Compressed";
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Unleashed", new Animation(atlas.findRegion("SpringHead/Unleashed"), 6, .1f));
            put("Compressed", new Animation(atlas.findRegion("SpringHead/Compressed")));
        }});
    }

}
