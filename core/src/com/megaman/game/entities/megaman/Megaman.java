package com.megaman.game.entities.megaman;

import com.badlogic.gdx.audio.Sound;
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
import com.megaman.game.entities.megaman.animations.MegamanAnimator;
import com.megaman.game.entities.megaman.health.MegamanHealthHandler;
import com.megaman.game.entities.megaman.vals.AButtonTask;
import com.megaman.game.entities.megaman.weapons.MegamanWeapon;
import com.megaman.game.entities.megaman.weapons.MegamanWeaponHandler;
import com.megaman.game.entities.projectiles.ChargeStatus;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.events.EventType;
import com.megaman.game.health.HealthComponent;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.ShapeUtils;
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

    public static final float RUN_SPEED = 4f;
    public static final float RUN_IMPULSE = 50f;
    public static final float WATER_RUN_SPEED = 2.25f;

    public static final float JUMP_VEL = 17.5f;

    public static final float WALL_JUMP_VEL = 30f;
    public static final float WALL_JUMP_HORIZ = 12f;
    public static final float WALL_JUMP_IMPETUS_TIME = .2f;

    public static final float AIR_DASH_VEL = 12f;
    public static final float AIR_DASH_END_BUMP = 3f;
    public static final float WATER_AIR_DASH_VEL = 6f;
    public static final float WATER_AIR_DASH_END_BUMP = 2f;
    public static final float MAX_AIR_DASH_TIME = .25f;

    public static final float GROUND_SLIDE_VEL = 12f;
    public static final float WATER_GROUND_SLIDE_VEL = 6f;
    public static final float MAX_GROUND_SLIDE_TIME = .35f;

    public static final float GROUNDED_GRAVITY = -.125f;
    public static final float UNGROUNDED_GRAVITY = -.5f;
    public static final float WATER_UNGROUNDED_GRAVITY = -.25f;

    public static final float SHOOT_ANIM_TIME = .5f;

    public static final float DAMAGE_DURATION = .75f;
    public static final float DAMAGE_RECOVERY_TIME = 1.5f;
    public static final float DAMAGE_RECOVERY_FLASH_DURATION = .05f;

    public static final float TIME_TO_HALFWAY_CHARGED = .5f;
    public static final float TIME_TO_FULLY_CHARGED = 1.25f;

    public static final float EXPLOSION_ORB_SPEED = 3.5f;

    public static final float CHARGING_ANIM_TIME = .125f;

    private static final Map<Class<? extends Damager>, DamageNegotiation> dmgNegs = new HashMap<>() {{

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
        chargingTimer = new Timer(
                TIME_TO_FULLY_CHARGED,
                new TimeMarkedRunnable(TIME_TO_HALFWAY_CHARGED,
                        () -> request(SoundAsset.MEGA_BUSTER_CHARGING_SOUND, true)));
        currWeapon = MegamanWeapon.MEGA_BUSTER;
        weaponHandler = new MegamanWeaponHandler(this);
        weaponHandler.putWeapon(MegamanWeapon.MEGA_BUSTER);
        weaponHandler.putWeapon(MegamanWeapon.FLAME_TOSS);
        healthHandler = new MegamanHealthHandler(this);
        putComponent(updatableComponent());
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(healthComponent());
        putComponent(behaviorComponent());
        putComponent(new SoundComponent());
        putComponent(controllerComponent());
        putComponent(new AnimationComponent(MegamanAnimator.getAnimator(this)));
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
        request(SoundAsset.MEGAMAN_DAMAGE_SOUND);
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
        stopLoop(SoundAsset.MEGA_BUSTER_CHARGING_SOUND);
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

    public boolean isDamaged() {
        return !dmgTimer.isFinished();
    }

    public void request(SoundAsset s) {
        request(s, false);
    }

    public void request(SoundAsset s, boolean loop) {
        getComponent(SoundComponent.class).request(s, loop);
    }

    public void stopLoop(SoundAsset s) {
        getComponent(SoundComponent.class).stopLoop(s);
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
                    getComponent(SoundComponent.class).request(SoundAsset.ERROR_SOUND);
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
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY);
        body.fixtures.add(bodyFixture);
        shapeHandles.add(new ShapeHandle(bodyFixture.bounds, Color.BLUE));
        Fixture feetFixture = new Fixture(this, FixtureType.FEET, m1);
        feetFixture.offset.y = -WorldVals.PPM / 2f;
        feetFixture.putUserData(ConstKeys.RUN, onBounce);
        body.fixtures.add(feetFixture);
        shapeHandles.add(new ShapeHandle(feetFixture.bounds, Color.GREEN));
        Fixture headFixture = new Fixture(this, FixtureType.HEAD, m1);
        headFixture.offset.y = WorldVals.PPM / 2f;
        headFixture.putUserData(ConstKeys.RUN, onBounce);
        body.fixtures.add(headFixture);
        shapeHandles.add(new ShapeHandle(headFixture.bounds, Color.ORANGE));
        Rectangle m2 = new Rectangle();
        m2.setSize(WorldVals.PPM / 16f, WorldVals.PPM / 16f);
        Fixture leftFixture = new Fixture(this, FixtureType.SIDE, m2);
        leftFixture.offset.set(-.4f * WorldVals.PPM, .15f * WorldVals.PPM);
        leftFixture.putUserData(ConstKeys.RUN, onBounce);
        leftFixture.putUserData(ConstKeys.SIDE, ConstKeys.LEFT);
        body.fixtures.add(leftFixture);
        shapeHandles.add(new ShapeHandle(leftFixture.bounds, Color.PINK));
        Fixture rightFixture = new Fixture(this, FixtureType.SIDE, m2);
        rightFixture.offset.set(.4f * WorldVals.PPM, .15f * WorldVals.PPM);
        rightFixture.putUserData(ConstKeys.RUN, onBounce);
        rightFixture.putUserData(ConstKeys.SIDE, ConstKeys.RIGHT);
        body.fixtures.add(rightFixture);
        shapeHandles.add(new ShapeHandle(rightFixture.bounds, Color.PINK));
        Fixture hitboxFixture = new Fixture(this, FixtureType.DAMAGEABLE, .8f * WorldVals.PPM);
        body.fixtures.add(hitboxFixture);
        shapeHandles.add(new ShapeHandle(hitboxFixture.bounds, Color.RED));
        UpdatableComponent u = getComponent(UpdatableComponent.class);
        u.add(delta -> {
            if (is(BehaviorType.GROUND_SLIDING)) {
                body.bounds.height = .45f * WorldVals.PPM;
                feetFixture.offset.y = -WorldVals.PPM / 4f;
            } else {
                body.bounds.height = .95f * WorldVals.PPM;
                feetFixture.offset.y = -WorldVals.PPM / 2f;
            }
            bodyFixture.bounds.set(body.bounds);
            if (body.velocity.y < 0f && !body.is(BodySense.FEET_ON_GROUND)) {
                body.gravity.y =
                        (is(BodySense.IN_WATER) ? WATER_UNGROUNDED_GRAVITY : UNGROUNDED_GRAVITY) * WorldVals.PPM;
            } else {
                body.gravity.y = GROUNDED_GRAVITY * WorldVals.PPM;
                if (body.is(BodySense.IN_WATER)) {
                    body.gravity.y *= 1.75f;
                }
            }
        });
        putComponent(new ShapeComponent(shapeHandles));
        return new BodyComponent(body);
    }

    private HealthComponent healthComponent() {
        return new HealthComponent(() -> {
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

                // game.getGameEngine().spawnEntity(new ExplosionOrb(game, body, traj));
            }
            game.getEventMan().dispatchEvent(new Event(EventType.PLAYER_DEAD));
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.65f * WorldVals.PPM, 1.25f * WorldVals.PPM);
        SpriteHandle handle = new SpriteHandle(sprite);
        handle.priority = 3;
        handle.updatable = delta -> {
            handle.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setAlpha(recoveryBlink ? 0f : 1f);
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
                Sound swimSound = game.getAssMan().getSound(SoundAsset.SWIM_SOUND);
                game.getAudioMan().playSound(swimSound, false);
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
                boolean wallSliding = is(BehaviorType.WALL_SLIDING);
                float x;
                if (wallSliding) {
                    x = WALL_JUMP_HORIZ * WorldVals.PPM;
                    if (is(Facing.LEFT)) {
                        x *= -1f;
                    }
                } else {
                    x = body.velocity.x;
                }
                float y = (wallSliding ? WALL_JUMP_VEL : JUMP_VEL) * WorldVals.PPM;
                body.velocity.set(x, y);
                if (wallSliding) {
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
                body.gravityOn = false;
                aButtonTask = AButtonTask.JUMP;
                request(SoundAsset.WHOOSH_SOUND);
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
                stopLoop(SoundAsset.MEGA_BUSTER_CHARGING_SOUND);
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
