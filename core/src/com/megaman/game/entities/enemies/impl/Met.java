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
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.*;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.projectiles.ProjectileFactory;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.entities.projectiles.impl.Fireball;
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

public class Met extends Enemy implements Faceable {

    private enum MetBehavior {
        SHIELDING,
        POP_UP,
        RUNNING
    }

    private static final float SHIELDING_DUR = 1.15f;
    private static final float RUNNING_DUR = .5f;
    private static final float POP_UP_DUR = .5f;
    private static final float RUN_VEL = 8f;
    private static final float RUN_IN_WATER_VEL = 3f;
    private static final float GRAVITY_Y = -.15f;
    private static final float BULLET_TRAJ_X = 15f;
    private static final float BULLET_TRAJ_Y = .25f;
    private static final float VEL_CLAMP_X = 8f;
    private static final float VEL_CLAMP_Y = 1.5f;

    private final Timer[] metBehavTimers;
    private final Sprite sprite;

    private MetBehavior behav;
    @Getter
    @Setter
    private Facing facing;
    private String type;

    public Met(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        metBehavTimers = new Timer[]{
                new Timer(SHIELDING_DUR),
                new Timer(RUNNING_DUR),
                new Timer(POP_UP_DUR)
        };
        type = "";
        sprite = new Sprite();
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        setBehav(MetBehavior.SHIELDING);
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
        type = data.containsKey(ConstKeys.TYPE) ? (String) data.get(ConstKeys.TYPE) : "";
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(10));
            put(Fireball.class, new DamageNegotiation(15));
            put(ChargedShot.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShotExplosion.class, new DamageNegotiation(15));
        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.gravityOn = true;
        body.bounds.setSize(.75f * WorldVals.PPM);
        body.velClamp.set(VEL_CLAMP_X * WorldVals.PPM, VEL_CLAMP_Y * WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY,
                new Rectangle().setSize(.75f * WorldVals.PPM));
        h.add(new ShapeHandle(bodyFixture.shape, Color.ORANGE));
        body.add(bodyFixture);

        // feet fixture
        Fixture feetFixture = new Fixture(this, FixtureType.FEET,
                new Rectangle().setSize(.15f * WorldVals.PPM, .2f * WorldVals.PPM));
        feetFixture.offset.y = -.375f * WorldVals.PPM;
        h.add(new ShapeHandle(feetFixture.shape, Color.GREEN));
        body.add(feetFixture);

        // shield fixture
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(WorldVals.PPM, .5f * WorldVals.PPM));
        shieldFixture.putUserData(ConstKeys.REFLECT, ConstKeys.UP);
        h.add(new ShapeHandle(shieldFixture.shape, () -> shieldFixture.active ? Color.BLUE : Color.GRAY));
        body.add(shieldFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(.75f * WorldVals.PPM));
        h.add(new ShapeHandle(damageableFixture.shape, () -> damageableFixture.active ? Color.RED : Color.GRAY));
        body.add(damageableFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.75f * WorldVals.PPM));
        body.add(damagerFixture);

        // pre-process
        body.preProcess = delta -> {
            body.gravity.y = is(BodySense.FEET_ON_GROUND) ? 0f : GRAVITY_Y * WorldVals.PPM;
            shieldFixture.active = behav == MetBehavior.SHIELDING;
            damageableFixture.active = !shieldFixture.active;
        };

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            if (game.getMegaman().dead) {
                return;
            }
            switch (behav) {
                case SHIELDING -> {
                    Timer shieldingTimer = metBehavTimers[MetBehavior.SHIELDING.ordinal()];
                    if (!isPlayerShootingAtMe()) {
                        shieldingTimer.update(delta);
                    }
                    if (shieldingTimer.isFinished()) {
                        setBehav(MetBehavior.POP_UP);
                    }
                }
                case POP_UP -> {
                    setFacing(game.getMegaman().body.isRightOf(body) ? Facing.RIGHT : Facing.LEFT);
                    Timer popupTimer = metBehavTimers[MetBehavior.POP_UP.ordinal()];
                    if (popupTimer.isAtBeginning()) {
                        shoot();
                    }
                    popupTimer.update(delta);
                    if (popupTimer.isFinished()) {
                        setBehav(MetBehavior.RUNNING);
                    }
                }
                case RUNNING -> {
                    Timer runningTimer = metBehavTimers[MetBehavior.RUNNING.ordinal()];
                    body.velocity.x = (is(BodySense.BODY_IN_WATER) ? RUN_IN_WATER_VEL : RUN_VEL) * WorldVals.PPM;
                    if (is(Facing.LEFT)) {
                        body.velocity.x *= -1f;
                    }
                    runningTimer.update(delta);
                    if (runningTimer.isFinished()) {
                        if (is(BodySense.FEET_ON_GROUND)) {
                            body.velocity.x = 0f;
                        }
                        setBehav(MetBehavior.SHIELDING);
                    }
                }
            }
        });
    }

    private void setBehav(MetBehavior behav) {
        this.behav = behav;
        for (int i = 0; i < metBehavTimers.length; i++) {
            metBehavTimers[i].reset();
        }
    }

    private void shoot() {
        Vector2 traj = new Vector2(BULLET_TRAJ_X, BULLET_TRAJ_Y);
        if (is(Facing.LEFT)) {
            traj.x *= -1f;
        }
        float offset = WorldVals.PPM / 64f;
        Vector2 spawn = new Vector2().set(body.getCenter()).add(is(Facing.LEFT) ? -offset : offset, offset);
        ObjectMap<String, Object> data = new ObjectMap<>();
        data.put(ConstKeys.OWNER, this);
        data.put(ConstKeys.TRAJECTORY, traj);
        Entity e = game.getEntityFactories().fetch(EntityType.PROJECTILE, ProjectileFactory.BULLET);
        game.getGameEngine().spawn(e, spawn, data);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> type + switch (behav) {
            case RUNNING -> "Run";
            case POP_UP -> "PopUp";
            case SHIELDING -> "LayDown";
        };
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Run", new Animation(atlas.findRegion("Met/Run"), 2, .125f));
            put("PopUp", new Animation(atlas.findRegion("Met/PopUp")));
            put("LayDown", new Animation(atlas.findRegion("Met/LayDown")));
            put("SnowRun", new Animation(atlas.findRegion("SnowMet/Run"), 2, .125f));
            put("SnowPopUp", new Animation(atlas.findRegion("SnowMet/PopUp")));
            put("SnowLayDown", new Animation(atlas.findRegion("SnowMet/LayDown")));
        }});
    }

}
