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
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.entities.*;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.projectiles.ProjectileFactory;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.entities.projectiles.impl.Fireball;
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

public class SniperJoe extends Enemy implements Faceable {

    private static final float[] TIMES_TO_SHOOT = new float[]{.15f, .75f, 1.35f};

    private static final float BULLET_SPEED = 7.5f;
    private static final float SHIELD_DUR = 1.75f;
    private static final float DAMAGE_DUR = .15f;
    private static final float SHOOT_DUR = 1.5f;

    private final Timer shieldTimer;
    private final Timer shootTimer;
    private final Sprite sprite;

    private boolean shielded;
    @Getter
    @Setter
    private Facing facing;

    public SniperJoe(MegamanGame game) {
        super(game, DAMAGE_DUR, BodyType.DYNAMIC);
        sprite = new Sprite();
        shieldTimer = new Timer(SHIELD_DUR);
        shootTimer = new Timer(SHOOT_DUR, new Array<>() {{
            for (float time : TIMES_TO_SHOOT) {
                add(new TimeMarkedRunnable(time, SniperJoe.this::shoot));
            }
        }});
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        shieldTimer.setToEnd();
        shootTimer.setToEnd();
        shielded = true;
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(5));
            put(Fireball.class, new DamageNegotiation(15));
            put(ChargedShot.class, new DamageNegotiation(damager ->
                    ((ChargedShot) damager).isFullyCharged() ? 15 : 10));
            put(ChargedShotExplosion.class, new DamageNegotiation(damager ->
                    ((ChargedShotExplosion) damager).isFullyCharged() ? 15 : 10));
        }};
    }

    @Override
    protected void defineBody(Body body) {
        Array<ShapeHandle> h = new Array<>();
        body.bounds.setSize(WorldVals.PPM, 1.25f * WorldVals.PPM);
        body.gravity.y = -WorldVals.PPM / 2f;
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.75f * WorldVals.PPM, 1.15f * WorldVals.PPM));
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(.8f * WorldVals.PPM, 1.35f * WorldVals.PPM));
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(.4f * WorldVals.PPM, .9f * WorldVals.PPM));
        shieldFixture.putUserData(ConstKeys.REFLECT, ConstKeys.STRAIGHT);
        h.add(new ShapeHandle(shieldFixture.shape, () -> shielded ? Color.GREEN : Color.GRAY));
        body.add(shieldFixture);
        body.preProcess = delta -> {
            shieldFixture.active = shielded;
            if (shielded) {
                damageableFixture.offset.x = (is(Facing.LEFT) ? .25f : -.25f) * WorldVals.PPM;
                shieldFixture.offset.x = (is(Facing.LEFT) ? -.35f : .35f) * WorldVals.PPM;
            } else {
                damageableFixture.offset.setZero();
            }
        };
        putComponent(new ShapeComponent(h));
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            setFacing(game.getMegaman().body.isRightOf(body) ? Facing.RIGHT : Facing.LEFT);
            Timer t = shielded ? shieldTimer : shootTimer;
            t.update(delta);
            if (t.isFinished()) {
                setShielded(!shielded);
            }
        });
    }

    private void shoot() {
        Vector2 traj = new Vector2(BULLET_SPEED * WorldVals.PPM, 0f);
        if (is(Facing.LEFT)) {
            traj.x *= -1f;
        }
        Vector2 spawn = new Vector2()
                .set(body.getCenter())
                .add(is(Facing.LEFT) ? -.2f : .2f * WorldVals.PPM, -.15f * WorldVals.PPM);
        Bullet bullet = (Bullet) game.getEntityFactories().fetch(EntityType.PROJECTILE, ProjectileFactory.BULLET);
        ObjectMap<String, Object> data = new ObjectMap<>();
        data.put(ConstKeys.OWNER, this);
        data.put(ConstKeys.TRAJECTORY, traj);
        game.getGameEngine().spawn(bullet, spawn, data);
        getComponent(SoundComponent.class).requestToPlay(SoundAsset.ENEMY_BULLET_SOUND);
    }

    private void setShielded(boolean shielded) {
        this.shielded = shielded;
        (shielded ? shieldTimer : shootTimer).reset();
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.35f * WorldVals.PPM, 1.35f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> shielded ? "Shielded" : "Shooting";
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Shooting", new Animation(atlas.findRegion("SniperJoe/Shooting")));
            put("Shielded", new Animation(atlas.findRegion("SniperJoe/Shielded")));
        }});
    }

}
