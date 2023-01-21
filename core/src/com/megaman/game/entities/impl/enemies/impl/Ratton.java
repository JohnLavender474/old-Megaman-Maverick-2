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
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Ratton extends Enemy implements Faceable {

    private static final float STAND_DUR = .75f;
    private static final float G_GRAV = -.0015f;
    private static final float GRAV = -.375f;
    private static final float JUMP_X = 15f;
    private static final float JUMP_Y = 18f;

    private final Sprite sprite;
    private final Timer standTimer;

    @Getter
    @Setter
    private Facing facing;

    public Ratton(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        sprite = new Sprite();
        standTimer = new Timer(STAND_DUR);
        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
        standTimer.reset();
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

    protected void defineBody() {
        body.gravityOn = true;
        body.affectedByResistance = true;
        body.bounds.setSize(WorldVals.PPM, WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY,
                new Rectangle().setSize(WorldVals.PPM, WorldVals.PPM));
        h.add(new ShapeHandle(bodyFixture.shape, Color.BLUE));
        body.add(bodyFixture);

        // feet fixture
        Fixture feetFixture = new Fixture(this, FixtureType.FEET,
                new Rectangle().setSize(WorldVals.PPM, .2f * WorldVals.PPM));
        feetFixture.offset.y = -.5f * WorldVals.PPM;
        h.add(new ShapeHandle(feetFixture.shape, Color.GREEN));
        body.add(feetFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(WorldVals.PPM, WorldVals.PPM));
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(WorldVals.PPM, WorldVals.PPM));
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }

        // pre-process
        body.preProcess = delta -> body.gravity.y = ((is(BodySense.FEET_ON_GROUND)) ? G_GRAV : GRAV) * WorldVals.PPM;
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            if (is(BodySense.FEET_ON_GROUND)) {
                standTimer.update(delta);
                setFacing(game.getMegaman().body.isRightOf(body) ? Facing.RIGHT : Facing.LEFT);
            }
            if (standTimer.isFinished()) {
                standTimer.reset();
                body.velocity.x += (is(Facing.LEFT) ? -JUMP_X : JUMP_X) * WorldVals.PPM;
                body.velocity.y += JUMP_Y * WorldVals.PPM;
            }
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(2f * WorldVals.PPM, 1.75f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> is(BodySense.FEET_ON_GROUND) ? "Stand" : "Jump";
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Stand", new Animation(atlas.findRegion("Ratton/Stand"), new float[]{1.5f, .15f}));
            put("Jump", new Animation(atlas.findRegion("Ratton/Jump"), 2, .1f, false));
        }});
    }

}
