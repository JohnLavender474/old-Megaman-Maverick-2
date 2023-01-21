package com.megaman.game.entities.impl.enemies.impl;

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
import com.megaman.game.entities.utils.damage.DamageNegotiation;
import com.megaman.game.entities.utils.damage.Damager;
import com.megaman.game.entities.utils.faceable.Faceable;
import com.megaman.game.entities.utils.faceable.Facing;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.impl.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.impl.projectiles.impl.Bullet;
import com.megaman.game.entities.impl.projectiles.impl.ChargedShot;
import com.megaman.game.entities.impl.projectiles.impl.Fireball;
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

public class FlyBoy extends Enemy implements Faceable {

    public static final float STAND_DUR = .5f;
    public static final float FLY_DUR = 1.5f;
    public static final float FLY_VEL = 10f;
    public static final float GRAV = -.2f;
    public static final float G_GRAV = -.015f;

    private final Sprite sprite;
    private final Timer flyTimer;
    private final Timer standTimer;

    @Getter
    @Setter
    private Facing facing;

    public FlyBoy(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        sprite = new Sprite();
        flyTimer = new Timer(FLY_DUR, true);
        standTimer = new Timer(STAND_DUR, true);
        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
        runOnDeath.add(() -> {
            if (hasHealth(0)) {
                explode();
            }
        });
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
        standTimer.reset();
        flyTimer.setToEnd();
    }

    public boolean isFlying() {
        return !flyTimer.isFinished();
    }

    public boolean isStanding() {
        return !standTimer.isFinished();
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(5));
            put(Fireball.class, new DamageNegotiation(10));
            put(ChargedShot.class, new DamageNegotiation(10));
            put(ChargedShotExplosion.class, new DamageNegotiation(5));
        }};
    }

    protected void defineBody() {
        body.bounds.setSize(WorldVals.PPM, 2f * WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY, new Rectangle(body.bounds));
        body.add(bodyFixture);
        h.add(new ShapeHandle(bodyFixture.shape, Color.BLUE));

        // feet fixture
        Fixture feetFixture = new Fixture(this, FixtureType.FEET,
                new Rectangle().setSize(.5f * WorldVals.PPM));
        feetFixture.offset.y = -WorldVals.PPM;
        body.add(feetFixture);
        h.add(new ShapeHandle(feetFixture.shape, Color.GREEN));

        // head fixture
        Fixture headFixture = new Fixture(this, FixtureType.HEAD,
                new Rectangle().setSize(.5f * WorldVals.PPM));
        headFixture.offset.y = WorldVals.PPM;
        body.add(headFixture);
        h.add(new ShapeHandle(headFixture.shape, Color.YELLOW));

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.8f * WorldVals.PPM, 1.5f * WorldVals.PPM));
        body.add(damagerFixture);
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Rectangle(body.bounds));
        body.add(damageableFixture);
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));

        // pre-process
        body.preProcess = delta -> {
            body.gravityOn = isStanding();
            body.gravity.y = (is(BodySense.FEET_ON_GROUND) ? G_GRAV : GRAV) * WorldVals.PPM;
        };

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            setFacing(body.isRightOf(game.getMegaman().body) ? Facing.LEFT : Facing.RIGHT);
            if (is(BodySense.FEET_ON_GROUND)) {
                body.velocity.x = 0f;
            }
            if (isStanding() && is(BodySense.FEET_ON_GROUND)) {
                standTimer.update(delta);
                if (standTimer.isJustFinished()) {
                    flyTimer.reset();
                    body.velocity.y = FLY_VEL * WorldVals.PPM;
                }
            }
            if (isFlying()) {
                flyTimer.update(delta);
                if (is(BodySense.HEAD_TOUCHING_BLOCK) || flyTimer.isJustFinished()) {
                    flyTimer.setToEnd();
                    standTimer.reset();
                    impulseToPlayer();
                }
            }
        });
    }

    private void impulseToPlayer() {
        body.velocity.x = 1.85f * (game.getMegaman().body.getX() - body.getX());
        body.velocity.y = 0f;
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(2.25f * WorldVals.PPM, 1.85f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> isFlying() ? "Fly" : "Stand";
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Fly", new Animation(atlas.findRegion("FlyBoy/Fly"), 4, .05f));
            put("Stand", new Animation(atlas.findRegion("FlyBoy/Stand")));
        }});
    }

}
