package com.megaman.game.entities.megaman;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.behaviors.Behavior;
import com.megaman.game.behaviors.BehaviorComponent;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.controllers.ControllerAdapter;
import com.megaman.game.controllers.ControllerBtn;
import com.megaman.game.controllers.ControllerComponent;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.entities.*;
import com.megaman.game.entities.enemies.impl.*;
import com.megaman.game.entities.explosions.ExplosionFactory;
import com.megaman.game.entities.explosions.impl.ExplosionOrb;
import com.megaman.game.entities.hazards.impl.LaserBeamer;
import com.megaman.game.entities.megaman.animations.MegamanAnimator;
import com.megaman.game.entities.megaman.health.MegamanHealthHandler;
import com.megaman.game.entities.megaman.vals.AButtonTask;
import com.megaman.game.entities.megaman.weapons.MegamanWeapon;
import com.megaman.game.entities.megaman.weapons.MegamanWeaponHandler;
import com.megaman.game.entities.projectiles.ChargeStatus;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.entities.projectiles.impl.Fireball;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.events.EventType;
import com.megaman.game.health.HealthComponent;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Positional;
import com.megaman.game.utils.objs.TimeMarkedRunnable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Megaman extends Entity implements Damageable, Faceable, Positional, EventListener {

    public static final float CLAMP_X = 25f;
    public static final float CLAMP_Y = 35f;

    public static final float RUN_SPEED = 5f;
    public static final float RUN_IMPULSE = 50f;
    public static final float WATER_RUN_SPEED = 2.25f;

    public static final float JUMP_VEL = 24f;
    public static final float WATER_JUMP_VEL = 15f;
    public static final float WALL_JUMP_VEL = 42f;
    public static final float WALL_JUMP_HORIZ = 12.5f;
    public static final float WALL_JUMP_IMPETUS_TIME = .1f;
    public static final float GROUNDED_GRAVITY = -.0015f;
    public static final float GRAVITY = -.375f;
    public static final float ICE_GRAVITY = -1f;
    public static final float WATER_GRAVITY = -.25f;
    public static final float WATER_ICE_GRAVITY = -.4f;

    public static final float AIR_DASH_VEL = 12f;
    public static final float AIR_DASH_END_BUMP = 3f;
    public static final float WATER_AIR_DASH_VEL = 6f;
    public static final float WATER_AIR_DASH_END_BUMP = 2f;
    public static final float MAX_AIR_DASH_TIME = .25f;

    public static final float GROUND_SLIDE_VEL = 12f;
    public static final float WATER_GROUND_SLIDE_VEL = 6f;
    public static final float MAX_GROUND_SLIDE_TIME = .35f;

    public static final float DAMAGE_DURATION = .75f;
    public static final float DAMAGE_RECOVERY_TIME = 1.5f;
    public static final float DAMAGE_RECOVERY_FLASH_DURATION = .05f;

    public static final float TIME_TO_HALFWAY_CHARGED = .5f;
    public static final float TIME_TO_FULLY_CHARGED = 1.25f;

    public static final float EXPLOSION_ORB_SPEED = 3.5f;

    public static final float SHOOT_ANIM_TIME = .5f;
    public static final float CHARGING_ANIM_TIME = .125f;

    private static final Map<Class<? extends Damager>, DamageNegotiation> dmgNegs = new HashMap<>() {{
        put(Bat.class, new DamageNegotiation(5));
        put(Met.class, new DamageNegotiation(5));
        put(MagFly.class, new DamageNegotiation(5));
        put(Bullet.class, new DamageNegotiation(10));
        put(ChargedShot.class, new DamageNegotiation(15));
        put(Fireball.class, new DamageNegotiation(5));
        put(Dragonfly.class, new DamageNegotiation(5));
        put(Matasaburo.class, new DamageNegotiation(5));
        put(SniperJoe.class, new DamageNegotiation(10));
        put(SpringHead.class, new DamageNegotiation(5));
        put(FloatingCan.class, new DamageNegotiation(10));
        put(LaserBeamer.class, new DamageNegotiation(10));
        put(SuctionRoller.class, new DamageNegotiation(10));
        put(GapingFish.class, new DamageNegotiation(5));
    }};

    public final MegamanWeaponHandler weaponHandler;
    public final MegamanHealthHandler healthHandler;
    public final Sprite sprite;
    public final Body body;

    private final Timer dmgTimer;
    private final Timer airDashTimer;
    private final Timer dmgRecovTimer;
    private final Timer chargingTimer;
    private final Timer wallJumpTimer;
    private final Timer shootAnimTimer;
    private final Timer groundSlideTimer;
    private final Timer dmgRecovBlinkTimer;

    public MegamanWeapon currWeapon;
    public AButtonTask aButtonTask;
    @Getter
    @Setter
    public Facing facing;

    private boolean recoveryBlink;

    public Megaman(MegamanGame game) {
        super(game, EntityType.MEGAMAN);
        sprite = new Sprite();
        body = new Body(BodyType.DYNAMIC, true);
        airDashTimer = new Timer(MAX_AIR_DASH_TIME);
        dmgTimer = new Timer(DAMAGE_DURATION, true);
        shootAnimTimer = new Timer(SHOOT_ANIM_TIME, true);
        groundSlideTimer = new Timer(MAX_GROUND_SLIDE_TIME);
        dmgRecovTimer = new Timer(DAMAGE_RECOVERY_TIME, true);
        wallJumpTimer = new Timer(WALL_JUMP_IMPETUS_TIME, true);
        dmgRecovBlinkTimer = new Timer(DAMAGE_RECOVERY_FLASH_DURATION);
        chargingTimer = new Timer(TIME_TO_FULLY_CHARGED, new TimeMarkedRunnable(TIME_TO_HALFWAY_CHARGED,
                () -> request(SoundAsset.MEGA_BUSTER_CHARGING_SOUND, true)));
        currWeapon = MegamanWeapon.MEGA_BUSTER;
        weaponHandler = new MegamanWeaponHandler(this);
        weaponHandler.putWeapon(MegamanWeapon.MEGA_BUSTER);
        weaponHandler.putWeapon(MegamanWeapon.FLAME_TOSS);
        healthHandler = new MegamanHealthHandler(this);
        putComponent(updatableComponent());
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(behaviorComponent());
        putComponent(new SoundComponent());
        putComponent(controllerComponent());
        putComponent(new HealthComponent());
        putComponent(new AnimationComponent(MegamanAnimator.getAnimator(this)));
        runOnDeath.add(() -> {
            game.getAudioMan().stopSound(SoundAsset.MEGA_BUSTER_CHARGING_SOUND);
            if (getHealth() > 0f) {
                return;
            }
            Array<Vector2> trajs = new Array<>() {{
                add(new Vector2(-EXPLOSION_ORB_SPEED, 0f));
                add(new Vector2(-EXPLOSION_ORB_SPEED, EXPLOSION_ORB_SPEED));
                add(new Vector2(0f, EXPLOSION_ORB_SPEED));
                add(new Vector2(EXPLOSION_ORB_SPEED, EXPLOSION_ORB_SPEED));
                add(new Vector2(EXPLOSION_ORB_SPEED, 0f));
                add(new Vector2(EXPLOSION_ORB_SPEED, -EXPLOSION_ORB_SPEED));
                add(new Vector2(0f, -EXPLOSION_ORB_SPEED));
                add(new Vector2(-EXPLOSION_ORB_SPEED, -EXPLOSION_ORB_SPEED));
            }};
            for (Vector2 traj : trajs) {
                ExplosionOrb e = (ExplosionOrb)
                        game.getEntityFactories().fetch(EntityType.EXPLOSION, ExplosionFactory.EXPLOSION_ORB);
                game.getGameEngine().spawnEntity(e, ShapeUtils.getCenterPoint(body.bounds), new ObjectMap<>() {{
                    put(ConstKeys.TRAJECTORY, traj);
                }});
            }
            game.getEventMan().dispatchEvent(new Event(EventType.PLAYER_DEAD));
        });
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> spawnData) {
        body.bounds.setPosition(spawn);
        currWeapon = MegamanWeapon.MEGA_BUSTER;
        weaponHandler.reset();
        aButtonTask = AButtonTask.JUMP;
        facing = Facing.RIGHT;
        airDashTimer.reset();
        dmgTimer.setToEnd();
        shootAnimTimer.setToEnd();
        groundSlideTimer.reset();
        dmgRecovTimer.setToEnd();
        wallJumpTimer.setToEnd();
        dmgRecovBlinkTimer.reset();
        chargingTimer.reset();
    }

    @Override
    public Set<Class<? extends Damager>> getDamagerMaskSet() {
        return dmgNegs.keySet();
    }

    @Override
    public void takeDamageFrom(Damager damager) {
        DamageNegotiation damageNegotiation = dmgNegs.get(damager.getClass());
        dmgTimer.reset();
        damageNegotiation.runOnDamage();
        healthHandler.removeHealth(damageNegotiation.getDamage(damager));
        request(SoundAsset.MEGA_BUSTER_CHARGING_SOUND, false);
        request(SoundAsset.MEGAMAN_DAMAGE_SOUND, true);
    }

    @Override
    public boolean isInvincible() {
        return !dmgTimer.isFinished() || !dmgRecovTimer.isFinished();
    }

    public boolean isDamaged() {
        return !dmgTimer.isFinished();
    }

    @Override
    public Vector2 getPosition() {
        return ShapeUtils.getBottomCenterPoint(body.bounds);
    }

    @Override
    public void setPosition(float x, float y) {
        body.bounds.setPosition(x, y);
    }

    @Override
    public void listenForEvent(Event event) {
        switch (event.eventType) {
            case BEGIN_GAME_ROOM_TRANS, CONTINUE_GAME_ROOM_TRANS -> {
                body.velocity.set(Vector2.Zero);
                Vector2 pos = event.getInfo(ConstKeys.POS, Vector2.class);
                body.setPos(pos, Position.BOTTOM_CENTER);
            }
            case GATE_INIT_OPENING -> body.velocity.set(Vector2.Zero);
        }
    }

    public int getHealth() {
        return getComponent(HealthComponent.class).getHealth();
    }

    public int getCurrentAmmo() {
        if (currWeapon == MegamanWeapon.MEGA_BUSTER) {
            return Integer.MAX_VALUE;
        }
        return weaponHandler.getAmmo(currWeapon);
    }

    public void stopCharging() {
        chargingTimer.reset();
    }

    public boolean isShooting() {
        return !shootAnimTimer.isFinished();
    }

    public boolean shoot() {
        boolean shot = weaponHandler.fireWeapon(currWeapon, getChargeStatus());
        if (shot) {
            shootAnimTimer.reset();
        }
        return shot;
    }

    public boolean canFireWeapon(MegamanWeapon weapon) {
        return weaponHandler.canFireWeapon(weapon, getChargeStatus());
    }

    public boolean canFireCurrWeapon() {
        return canFireWeapon(currWeapon);
    }

    public ChargeStatus getChargeStatus() {
        if (isChargingFully()) {
            return ChargeStatus.FULLY_CHARGED;
        }
        return isCharging() ? ChargeStatus.HALF_CHARGED : ChargeStatus.NOT_CHARGED;
    }

    public boolean isChargingFully() {
        return weaponHandler.isChargeable(currWeapon) && chargingTimer.isFinished();
    }

    public boolean isHalfCharging() {
        return getChargeStatus() == ChargeStatus.HALF_CHARGED;
    }

    public boolean isCharging() {
        return weaponHandler.isChargeable(currWeapon) && chargingTimer.getTime() >= TIME_TO_HALFWAY_CHARGED;
    }

    public boolean is(BehaviorType behaviorType) {
        return getComponent(BehaviorComponent.class).is(behaviorType);
    }

    public boolean is(BodySense bodySense) {
        return body.is(bodySense);
    }

    public void request(SoundAsset ass, boolean play) {
        SoundComponent c = getComponent(SoundComponent.class);
        if (play) {
            c.requestToPlay(ass);
        } else {
            c.requestToStop(ass);
        }
    }

    private ControllerComponent controllerComponent() {
        ControllerComponent c = new ControllerComponent();
        BehaviorComponent bc = getComponent(BehaviorComponent.class);
        ControllerManager ctrlMan = game.getCtrlMan();
        c.ctrlAdapters.put(ControllerBtn.DPAD_LEFT, new ControllerAdapter() {
            @Override
            public void onPressContinued(float delta) {
                if (isDamaged() || ctrlMan.isPressed(ControllerBtn.DPAD_RIGHT)) {
                    return;
                }
                setFacing(is(BehaviorType.WALL_SLIDING) ? Facing.RIGHT : Facing.LEFT);
                bc.set(BehaviorType.RUNNING, !is(BehaviorType.WALL_SLIDING));
                float threshold = (body.is(BodySense.IN_WATER) ? WATER_RUN_SPEED : RUN_SPEED) * WorldVals.PPM;
                if (body.velocity.x > -threshold) {
                    body.velocity.x += -RUN_IMPULSE * delta * WorldVals.PPM;
                }
            }

            @Override
            public void onJustReleased() {
                if (!ctrlMan.isPressed(ControllerBtn.DPAD_RIGHT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }

            @Override
            public void onReleaseContinued() {
                if (!ctrlMan.isPressed(ControllerBtn.DPAD_RIGHT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }
        });
        c.ctrlAdapters.put(ControllerBtn.DPAD_RIGHT, new ControllerAdapter() {
            @Override
            public void onPressContinued(float delta) {
                if (isDamaged() || ctrlMan.isPressed(ControllerBtn.DPAD_LEFT)) {
                    return;
                }
                setFacing(is(BehaviorType.WALL_SLIDING) ? Facing.LEFT : Facing.RIGHT);
                bc.set(BehaviorType.RUNNING, !is(BehaviorType.WALL_SLIDING));
                float threshold = (body.is(BodySense.IN_WATER) ? WATER_RUN_SPEED : RUN_SPEED) * WorldVals.PPM;
                if (body.velocity.x < threshold) {
                    body.velocity.x += RUN_IMPULSE * delta * WorldVals.PPM;
                }
            }

            @Override
            public void onJustReleased() {
                if (!ctrlMan.isPressed(ControllerBtn.DPAD_LEFT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }

            @Override
            public void onReleaseContinued() {
                if (!ctrlMan.isPressed(ControllerBtn.DPAD_LEFT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }
        });
        c.ctrlAdapters.put(ControllerBtn.X, new ControllerAdapter() {
            @Override
            public void onPressContinued(float delta) {
                if (isDamaged()) {
                    stopCharging();
                    return;
                }
                if (!isCharging() && !weaponHandler.canFireWeapon(currWeapon, ChargeStatus.HALF_CHARGED)) {
                    stopCharging();
                    return;
                }
                if (isHalfCharging() && !weaponHandler.canFireWeapon(currWeapon, ChargeStatus.FULLY_CHARGED)) {
                    return;
                }
                chargingTimer.update(delta);
            }

            @Override
            public void onJustReleased() {
                if (!canFireCurrWeapon() || !shoot()) {
                    getComponent(SoundComponent.class).requestToPlay(SoundAsset.ERROR_SOUND);
                }
                stopCharging();
            }
        });
        c.ctrlAdapters.put(ControllerBtn.SELECT, new ControllerAdapter() {
            @Override
            public void onJustPressed() {
                int x = currWeapon.ordinal() + 1;
                if (x >= MegamanWeapon.values().length) {
                    x = 0;
                }
                currWeapon = MegamanWeapon.values()[x];
            }
        });
        return c;
    }

    private BodyComponent bodyComponent() {
        Array<ShapeHandle> shapeHandles = new Array<>();
        body.velClamp.set(CLAMP_X * WorldVals.PPM, CLAMP_Y * WorldVals.PPM);
        body.bounds.width = .8f * WorldVals.PPM;
        body.affectedByResistance = true;
        Rectangle m1 = new Rectangle();
        m1.setSize(.575f * WorldVals.PPM, WorldVals.PPM / 16f);
        Runnable onBounce = () -> {
            if (!body.is(BodySense.IN_WATER)) {
                aButtonTask = AButtonTask.AIR_DASH;
            }
        };
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY,
                new Rectangle().setWidth(.8f * WorldVals.PPM));
        body.fixtures.add(bodyFixture);
        shapeHandles.add(new ShapeHandle(bodyFixture.shape, Color.YELLOW));
        Fixture feetFixture = new Fixture(this, FixtureType.FEET, new Rectangle(m1));
        feetFixture.offset.y = -WorldVals.PPM / 2f;
        feetFixture.putUserData(ConstKeys.RUN, onBounce);
        body.fixtures.add(feetFixture);
        shapeHandles.add(new ShapeHandle(feetFixture.shape, Color.GREEN));
        Fixture headFixture = new Fixture(this, FixtureType.HEAD, new Rectangle(m1));
        headFixture.offset.y = WorldVals.PPM / 2f;
        headFixture.putUserData(ConstKeys.RUN, onBounce);
        body.fixtures.add(headFixture);
        shapeHandles.add(new ShapeHandle(headFixture.shape, Color.ORANGE));
        Rectangle m2 = new Rectangle().setSize(WorldVals.PPM / 6f, WorldVals.PPM / 2.25f);
        Fixture leftFixture = new Fixture(this, FixtureType.SIDE, new Rectangle(m2));
        leftFixture.offset.set(-.4f * WorldVals.PPM, .125f * WorldVals.PPM);
        leftFixture.putUserData(ConstKeys.RUN, onBounce);
        leftFixture.putUserData(ConstKeys.SIDE, ConstKeys.LEFT);
        body.fixtures.add(leftFixture);
        shapeHandles.add(new ShapeHandle(leftFixture.shape, Color.PINK));
        Fixture rightFixture = new Fixture(this, FixtureType.SIDE, new Rectangle(m2));
        rightFixture.offset.set(.4f * WorldVals.PPM, .125f * WorldVals.PPM);
        rightFixture.putUserData(ConstKeys.RUN, onBounce);
        rightFixture.putUserData(ConstKeys.SIDE, ConstKeys.RIGHT);
        body.fixtures.add(rightFixture);
        shapeHandles.add(new ShapeHandle(rightFixture.shape, Color.PINK));
        Fixture hitboxFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(.8f * WorldVals.PPM));
        body.fixtures.add(hitboxFixture);
        shapeHandles.add(new ShapeHandle(hitboxFixture.shape, Color.RED));
        Fixture waterListenerFixture = new Fixture(this, FixtureType.WATER_LISTENER,
                new Rectangle().setSize(.8f * WorldVals.PPM, WorldVals.PPM / 4f));
        shapeHandles.add(new ShapeHandle(waterListenerFixture.shape, Color.BLUE));
        body.fixtures.add(waterListenerFixture);
        body.preProcess = delta -> {
            if (is(BehaviorType.GROUND_SLIDING)) {
                body.bounds.height = .45f * WorldVals.PPM;
                feetFixture.offset.y = -WorldVals.PPM / 4f;
            } else {
                body.bounds.height = .95f * WorldVals.PPM;
                feetFixture.offset.y = -WorldVals.PPM / 2f;
            }
            ((Rectangle) bodyFixture.shape).set(body.bounds);
            boolean wallSlidingOnIce = is(BehaviorType.WALL_SLIDING) &&
                    (is(BodySense.TOUCHING_ICE_LEFT) || is(BodySense.TOUCHING_ICE_RIGHT));
            float gravityY;
            if (is(BodySense.IN_WATER)) {
                gravityY = wallSlidingOnIce ? WATER_ICE_GRAVITY : WATER_GRAVITY;
            } else if (wallSlidingOnIce) {
                gravityY = ICE_GRAVITY;
            } else {
                gravityY = is(BodySense.FEET_ON_GROUND) ? GROUNDED_GRAVITY : GRAVITY;
            }
            /*
            float gravityY = is(BodySense.IN_WATER) ?
                    (wallSlidingOnIce ? WATER_ICE_GRAVITY : WATER_GRAVITY) :
                    (wallSlidingOnIce ? ICE_GRAVITY : GRAVITY);
             */
            body.gravity.y = gravityY * WorldVals.PPM;
            // body.gravityOn = !is(BodySense.FEET_ON_GROUND) && !is(BehaviorType.AIR_DASHING);
        };
        putComponent(new ShapeComponent(shapeHandles));
        return new BodyComponent(body);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.65f * WorldVals.PPM, 1.25f * WorldVals.PPM);
        SpriteHandle handle = new SpriteHandle(sprite);
        handle.priority = 3;
        handle.updatable = delta -> {
            handle.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setAlpha(isInvincible() ? (recoveryBlink ? 0f : 1f) : 1f);
            sprite.setFlip(is(BehaviorType.WALL_SLIDING) ? is(Facing.RIGHT) : is(Facing.LEFT), sprite.isFlipY());
            sprite.translateY(is(BehaviorType.GROUND_SLIDING) ? -.1f * WorldVals.PPM : 0f);
        };
        return new SpriteComponent(handle);
    }

    private BehaviorComponent behaviorComponent() {
        BehaviorComponent c = new BehaviorComponent();
        ControllerManager ctrlMan = game.getCtrlMan();
        // wall slide
        Behavior wallSlide = new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                if (isDamaged() ||
                        c.is(BehaviorType.JUMPING) ||
                        is(BodySense.FEET_ON_GROUND) ||
                        !wallJumpTimer.isFinished() // ||
                    // TODO: Fix
                    // !stats.canUseSpecialAbility(MegamanAbility.WALL_JUMP)
                ) {
                    return false;
                }
                if (is(BodySense.TOUCHING_BLOCK_LEFT) && ctrlMan.isPressed(ControllerBtn.DPAD_LEFT)) {
                    return true;
                }
                return is(BodySense.TOUCHING_BLOCK_RIGHT) && ctrlMan.isPressed(ControllerBtn.DPAD_RIGHT);
            }

            @Override
            protected void init() {
                c.set(BehaviorType.WALL_SLIDING, true);
                aButtonTask = is(BodySense.IN_WATER) ? AButtonTask.SWIM : AButtonTask.JUMP;
            }

            @Override
            protected void act(float delta) {
                body.resistance.y += 1.25f;
            }

            @Override
            protected void end() {
                c.set(BehaviorType.WALL_SLIDING, false);
                if (!is(BodySense.IN_WATER)) {
                    aButtonTask = AButtonTask.AIR_DASH;
                }
            }
        };
        c.behaviors.add(wallSlide);
        // swim
        Behavior swim = new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                if (isDamaged() || !is(BodySense.IN_WATER) || is(BodySense.HEAD_TOUCHING_BLOCK)) {
                    return false;
                }
                if (c.is(BehaviorType.SWIMMING)) {
                    return body.velocity.y > 0f;
                }
                return ctrlMan.isJustPressed(ControllerBtn.A) && aButtonTask == AButtonTask.SWIM;
            }

            @Override
            protected void init() {
                float x = 0f;
                float y = 18f * WorldVals.PPM;
                if (is(BehaviorType.WALL_SLIDING)) {
                    x = WALL_JUMP_HORIZ * 1.15f * WorldVals.PPM;
                    if (is(Facing.LEFT)) {
                        x *= -1f;
                    }
                    y *= 2f;
                }
                body.velocity.add(x, y);
                c.set(BehaviorType.SWIMMING, true);
                game.getAudioMan().playSound(SoundAsset.SWIM_SOUND);
            }

            @Override
            protected void act(float delta) {
            }

            @Override
            protected void end() {
                c.set(BehaviorType.SWIMMING, false);
            }
        };
        c.behaviors.add(swim);
        // jump
        Behavior jump = new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                if (isDamaged() ||
                        is(BehaviorType.SWIMMING) ||
                        is(BodySense.HEAD_TOUCHING_BLOCK) ||
                        !ctrlMan.isPressed(ControllerBtn.A) ||
                        ctrlMan.isPressed(ControllerBtn.DPAD_DOWN)) {
                    return false;
                }
                return is(BehaviorType.JUMPING) ?
                        body.velocity.y >= 0f :
                        aButtonTask == AButtonTask.JUMP &&
                                ctrlMan.isJustPressed(ControllerBtn.A) &&
                                (is(BodySense.FEET_ON_GROUND) || is(BehaviorType.WALL_SLIDING));
            }

            @Override
            protected void init() {
                c.set(BehaviorType.JUMPING, true);
                Vector2 v = new Vector2();
                if (is(BehaviorType.WALL_SLIDING)) {
                    v.x = WALL_JUMP_HORIZ * WorldVals.PPM;
                    if (is(Facing.LEFT)) {
                        v.x *= -1f;
                    }
                } else {
                    v.x = body.velocity.x;
                }
                if (is(BodySense.IN_WATER)) {
                    v.y = WATER_JUMP_VEL * WorldVals.PPM;
                } else {
                    v.y = (is(BehaviorType.WALL_SLIDING) ? WALL_JUMP_VEL : JUMP_VEL) * WorldVals.PPM;
                }
                body.velocity.set(v);
                if (is(BehaviorType.WALL_SLIDING)) {
                    wallJumpTimer.reset();
                }
            }

            @Override
            protected void act(float delta) {
            }

            @Override
            protected void end() {
                c.set(BehaviorType.JUMPING, false);
                body.velocity.y = 0f;
            }
        };
        c.behaviors.add(jump);
        // air dash
        Behavior airDash = new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                if (isDamaged() ||
                        airDashTimer.isFinished() ||
                        is(BehaviorType.WALL_SLIDING) ||
                        is(BodySense.FEET_ON_GROUND) // ||
                    // TODO: fix
                    // !stats.canUseSpecialAbility(MegamanAbility.AIR_DASH)
                ) {
                    return false;
                }
                return is(BehaviorType.AIR_DASHING) ?
                        ctrlMan.isPressed(ControllerBtn.A) :
                        ctrlMan.isJustPressed(ControllerBtn.A) && aButtonTask == AButtonTask.AIR_DASH;
            }

            @Override
            protected void init() {

                // TODO: gravity on toggled only in body pre-process
                body.gravityOn = false;

                aButtonTask = AButtonTask.JUMP;
                request(SoundAsset.WHOOSH_SOUND, true);
                c.set(BehaviorType.AIR_DASHING, true);
            }

            @Override
            protected void act(float delta) {
                airDashTimer.update(delta);
                body.velocity.y = 0f;
                if ((is(Facing.LEFT) && is(BodySense.TOUCHING_BLOCK_LEFT)) ||
                        (is(Facing.RIGHT) && is(BodySense.TOUCHING_BLOCK_RIGHT))) {
                    return;
                }
                float x = (is(BodySense.IN_WATER) ? WATER_AIR_DASH_VEL : AIR_DASH_VEL) * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    x *= -1f;
                }
                body.velocity.x = x;
            }

            @Override
            protected void end() {
                airDashTimer.reset();

                // TODO: gravity on toggled only in body pre-process
                body.gravityOn = true;

                c.set(BehaviorType.AIR_DASHING, false);
                float x = (is(BodySense.IN_WATER) ? WATER_AIR_DASH_END_BUMP : AIR_DASH_END_BUMP) * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    x *= -1f;
                }
                body.velocity.x += x;
            }
        };
        c.behaviors.add(airDash);
        // ground slide
        Behavior groundSlide = new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                // TODO: Fix
                /*
                if (!stats.canUseSpecialAbility(MegamanAbility.GROUND_SLIDE)) {
                    return false;
                }
                 */
                if (is(BehaviorType.GROUND_SLIDING) && is(BodySense.HEAD_TOUCHING_BLOCK)) {
                    return true;
                }
                if (isDamaged() || groundSlideTimer.isFinished() || !is(BodySense.FEET_ON_GROUND)) {
                    return false;
                }
                if (!ctrlMan.isPressed(ControllerBtn.DPAD_DOWN)) {
                    return false;
                }
                return is(BehaviorType.GROUND_SLIDING) ?
                        ctrlMan.isPressed(ControllerBtn.A) :
                        ctrlMan.isJustPressed(ControllerBtn.A);
            }

            @Override
            protected void init() {
                c.set(BehaviorType.GROUND_SLIDING, true);
            }

            @Override
            protected void act(float delta) {
                groundSlideTimer.update(delta);
                if (isDamaged() ||
                        (is(Facing.LEFT) && is(BodySense.TOUCHING_BLOCK_LEFT)) ||
                        (is(Facing.RIGHT) && is(BodySense.TOUCHING_BLOCK_RIGHT))) {
                    return;
                }
                float x = (is(BodySense.IN_WATER) ? WATER_GROUND_SLIDE_VEL : GROUND_SLIDE_VEL) * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    x *= -1f;
                }
                body.velocity.x = x;
            }

            @Override
            protected void end() {
                groundSlideTimer.reset();
                c.set(BehaviorType.GROUND_SLIDING, false);
                float endDash = (is(BodySense.IN_WATER) ? 2f : 5f) * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    endDash *= -1;
                }
                body.velocity.x += endDash;
            }
        };
        c.behaviors.add(groundSlide);
        return c;
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            if (!weaponHandler.isChargeable(currWeapon)) {
                stopCharging();
            }
            weaponHandler.update(delta);
            dmgTimer.update(delta);
            if (isDamaged()) {
                chargingTimer.reset();
                float dmgX = .15f * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    dmgX *= -1f;
                }
                body.velocity.x += dmgX;
            }
            if (dmgTimer.isJustFinished()) {
                dmgRecovTimer.reset();
            }
            if (dmgTimer.isFinished() && !dmgRecovTimer.isFinished()) {
                dmgRecovTimer.update(delta);
                dmgRecovBlinkTimer.update(delta);
                if (dmgRecovBlinkTimer.isFinished()) {
                    recoveryBlink = !recoveryBlink;
                    dmgRecovBlinkTimer.reset();
                }
            }
            if (dmgRecovTimer.isJustFinished()) {
                recoveryBlink = false;
            }
            shootAnimTimer.update(delta);
            wallJumpTimer.update(delta);
        });
    }

}
