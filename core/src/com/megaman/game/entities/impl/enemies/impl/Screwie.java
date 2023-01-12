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
import com.megaman.game.entities.damage.DamageNegotiation;
import com.megaman.game.entities.damage.Damager;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.impl.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.impl.projectiles.ProjectileFactory;
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
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.TimeMarkedRunnable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Screwie extends Enemy {

    private static final Logger logger = new Logger(Screwie.class, MegamanGame.DEBUG && true);

    private static final float SHOOT_DUR = 2f;
    private static final float DOWN_DUR = 1f;
    private static final float RISE_DROP_DUR = .3f;
    private static final float BULLET_VEL = 10f;

    private final Sprite sprite;
    private final Timer downTimer;
    private final Timer riseTimer;
    private final Timer shootTimer;
    private final Timer dropTimer;

    private String type;
    @Getter
    private boolean upsideDown;

    public Screwie(MegamanGame game) {
        super(game, BodyType.ABSTRACT);
        sprite = new Sprite();
        downTimer = new Timer(DOWN_DUR);
        riseTimer = new Timer(RISE_DROP_DUR);
        shootTimer = new Timer(SHOOT_DUR, new Array<>() {{
            add(new TimeMarkedRunnable(.5f, () -> shoot()));
            add(new TimeMarkedRunnable(1f, () -> shoot()));
            add(new TimeMarkedRunnable(1.5f, () -> shoot()));
        }});
        dropTimer = new Timer(RISE_DROP_DUR);
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        type = (String) data.get(ConstKeys.TYPE);
        upsideDown = (boolean) data.get(ConstKeys.DOWN);
        downTimer.reset();
        riseTimer.setToEnd();
        shootTimer.setToEnd();
        dropTimer.setToEnd();
        Position p = upsideDown ? Position.TOP_CENTER : Position.BOTTOM_CENTER;
        Vector2 spawn = ShapeUtils.getPoint(bounds, p);
        ShapeUtils.setToPoint(body.bounds, spawn, p);
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
        body.bounds.setWidth(.5f * WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();
        h.add(new ShapeHandle(body.bounds, Color.GREEN));

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.15f * WorldVals.PPM));
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(.35f * WorldVals.PPM, .15f * WorldVals.PPM));
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);

        // pre-process
        body.preProcess = delta -> {
            body.bounds.height = (isShooting() ? .65f : .35f) * WorldVals.PPM;
        };

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            if (!downTimer.isFinished()) {
                downTimer.update(delta);
                if (downTimer.isFinished()) {
                    logger.log("Set to rise");
                    riseTimer.reset();
                }
            } else if (!riseTimer.isFinished()) {
                riseTimer.update(delta);
                if (riseTimer.isFinished()) {
                    logger.log("Set to shoot");
                    shootTimer.reset();
                }
            } else if (!shootTimer.isFinished()) {
                shootTimer.update(delta);
                if (shootTimer.isFinished()) {
                    logger.log("Set to drop");
                    dropTimer.reset();
                }
            } else if (!dropTimer.isFinished()) {
                dropTimer.update(delta);
                if (dropTimer.isFinished()) {
                    logger.log("Set to down");
                    downTimer.reset();
                }
            }
        });
    }

    private void shoot() {
        for (int i = 0; i < 2; i++) {
            Bullet b = (Bullet) game.getEntityFactories().fetch(EntityType.PROJECTILE, ProjectileFactory.BULLET);
            Vector2 spawn = new Vector2(body.getCenter());
            spawn.x += (i == 0 ? -.2f : .2f) * WorldVals.PPM;
            spawn.y += (upsideDown ? -.165f : .165f) * WorldVals.PPM;
            Vector2 traj = new Vector2();
            traj.x = i == 0 ? -BULLET_VEL : BULLET_VEL;
            game.getGameEngine().spawn(b, spawn, new ObjectMap<>() {{
                put(ConstKeys.TRAJECTORY, traj);
                put(ConstKeys.OWNER, Screwie.this);
            }});
        }
    }

    private boolean isDown() {
        return !downTimer.isFinished();
    }

    private boolean isShooting() {
        return !shootTimer.isFinished();
    }

    private boolean isRising() {
        return !riseTimer.isFinished();
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.35f * WorldVals.PPM, 1.35f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 3);
        h.updatable = delta -> {
            h.setPosition(body.bounds, isUpsideDown() ? Position.TOP_CENTER : Position.BOTTOM_CENTER);
            sprite.setFlip(false, isUpsideDown());
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> {
            String s = type;
            if (isDown()) {
                s += "Down";
            } else if (isShooting()) {
                s += "Shoot";
            } else if (isRising()) {
                s += "Rise";
            } else {
                s += "Drop";
            }
            return s;
        };
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("RedDown", new Animation(atlas.findRegion("RedScrewie/Down")));
            put("RedRise", new Animation(atlas.findRegion("RedScrewie/Rise"), 3, .1f, false));
            put("RedDrop", new Animation(atlas.findRegion("RedScrewie/Drop"), 3, .1f, false));
            put("RedShoot", new Animation(atlas.findRegion("RedScrewie/Shoot"), 3, .1f));
            put("BlueDown", new Animation(atlas.findRegion("BlueScrewie/Down")));
            put("BlueRise", new Animation(atlas.findRegion("BlueScrewie/Rise"), 3, .1f, false));
            put("BlueDrop", new Animation(atlas.findRegion("BlueScrewie/Drop"), 3, .1f, false));
            put("BlueShoot", new Animation(atlas.findRegion("BlueScrewie/Shoot"), 3, .1f));
        }});
    }

}
