package com.megaman.game.entities.impl.enemies.impl;

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
import com.megaman.game.entities.*;
import com.megaman.game.entities.utils.damage.DamageNegotiation;
import com.megaman.game.entities.utils.damage.Damager;
import com.megaman.game.entities.utils.faceable.Faceable;
import com.megaman.game.entities.utils.faceable.Facing;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.impl.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.impl.projectiles.ProjectileFactory;
import com.megaman.game.entities.impl.projectiles.impl.Bullet;
import com.megaman.game.entities.impl.projectiles.impl.ChargedShot;
import com.megaman.game.entities.impl.projectiles.impl.Fireball;
import com.megaman.game.entities.impl.projectiles.impl.Picket;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.TimeMarkedRunnable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PicketJoe extends Enemy implements Faceable {

    private static final float STAND_DUR = .4f;
    private static final float THROW_DUR = .5f;
    private static final float PICKET_IMPULSE_Y = 10f;

    @Getter
    @Setter
    private Facing facing;

    private final Sprite sprite;
    private final Timer standTimer;
    private final Timer throwTimer;

    public PicketJoe(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        sprite = new Sprite();
        standTimer = new Timer(STAND_DUR);
        throwTimer = new Timer(THROW_DUR, new TimeMarkedRunnable(.2f, this::throwPicket));
        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
        throwTimer.setToEnd();
        standTimer.reset();
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDmgNegs() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(5));
            put(Fireball.class, new DamageNegotiation(15));
            put(ChargedShot.class, new DamageNegotiation(damager ->
                    ((ChargedShot) damager).isFullyCharged() ? 15 : 10));
            put(ChargedShotExplosion.class, new DamageNegotiation(damager ->
                    ((ChargedShotExplosion) damager).isFullyCharged() ? 15 : 10));
        }};
    }

    protected void defineBody() {
        body.bounds.setSize(WorldVals.PPM, 1.25f * WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // shield fixture
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(.4f * WorldVals.PPM, .9f * WorldVals.PPM));
        shieldFixture.putUserData(ConstKeys.REFLECT, ConstKeys.STRAIGHT);
        h.add(new ShapeHandle(shieldFixture.shape, () -> isStanding() ? Color.GREEN : Color.GRAY));
        body.add(shieldFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.75f * WorldVals.PPM, 1.15f * WorldVals.PPM));
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(.8f * WorldVals.PPM, 1.35f * WorldVals.PPM));
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);

        // pre-process
        body.preProcess = delta -> {
            shieldFixture.active = isStanding();
            if (isStanding()) {
                damageableFixture.offset.x = (is(Facing.LEFT) ? .25f : -.25f) * WorldVals.PPM;
                shieldFixture.offset.x = (is(Facing.LEFT) ? -.35f : .35f) * WorldVals.PPM;
            } else {
                damageableFixture.offset.setZero();
            }
        };

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }
    }

    private boolean isStanding() {
        return !standTimer.isFinished();
    }

    private boolean isThrowingPicket() {
        return !throwTimer.isFinished();
    }

    private void setToStanding() {
        standTimer.reset();
        throwTimer.setToEnd();
    }

    private void setToThrowing() {
        standTimer.setToEnd();
        throwTimer.reset();
    }

    private void throwPicket() {
        Vector2 spawn = new Vector2(body.getCenter());
        spawn.x += (is(Facing.LEFT) ? -.1f : .1f) * WorldVals.PPM;
        spawn.y += .25f * WorldVals.PPM;
        Picket p = (Picket) game.getEntityFactories().fetch(EntityType.PROJECTILE, ProjectileFactory.PICKET);
        float impulseX = 1.15f * (game.getMegaman().body.getX() - body.getX());
        game.getGameEngine().spawn(p, spawn, new ObjectMap<>() {{
            put(ConstKeys.X, impulseX);
            put(ConstKeys.Y, PICKET_IMPULSE_Y * WorldVals.PPM);
        }});
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            if (isStanding()) {
                standTimer.update(delta);
                if (standTimer.isFinished()) {
                    setToThrowing();
                }
            } else if (isThrowingPicket()) {
                throwTimer.update(delta);
                if (throwTimer.isFinished()) {
                    setToStanding();
                }
            }
            if (throwTimer.isFinished()) {
                setFacing(game.getMegaman().body.isRightOf(body) ? Facing.RIGHT : Facing.LEFT);
            }
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.35f * WorldVals.PPM, 1.35f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> isStanding() ? "Stand" : "Throw";
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Stand", new Animation(atlas.findRegion("PicketJoe/Stand")));
            put("Throw", new Animation(atlas.findRegion("PicketJoe/Throw"), 3, .1f, false));
        }});
    }

}
