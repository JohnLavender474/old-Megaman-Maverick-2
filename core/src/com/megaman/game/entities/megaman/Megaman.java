package com.megaman.game.entities.megaman;

import com.badlogic.gdx.audio.Sound;
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
import com.megaman.game.animations.Animator;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.behaviors.Behavior;
import com.megaman.game.behaviors.BehaviorComponent;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.controllers.ControllerAdapter;
import com.megaman.game.controllers.ControllerBtn;
import com.megaman.game.controllers.ControllerComponent;
import com.megaman.game.entities.*;
import com.megaman.game.entities.megaman.health.MegamanHealthHandler;
import com.megaman.game.entities.megaman.weapons.MegaChargeStatus;
import com.megaman.game.entities.megaman.weapons.MegamanWeapon;
import com.megaman.game.entities.megaman.weapons.MegamanWeaponHandler;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.events.EventType;
import com.megaman.game.health.HealthComponent;
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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class Megaman extends Entity implements Damageable, Faceable, Positional, EventListener {

    public static final float CLAMP_X = 20f;
    public static final float CLAMP_Y = 35f;

    public static final float RUN_SPEED = 4f;
    public static final float WATER_RUN_SPEED = 2f;

    public static final float JUMP_VEL = 17.5f;
    public static final float WATER_JUMP_VEL = 25f;

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

    private static final Map<Class<? extends Damager>, DamageNegotiation> dmgNegs = new HashMap<>() {{

    }};

    public final MegamanWeaponHandler weaponHandler;
    public final MegamanHealthHandler healthHandler;

    public final Sprite sprite = new Sprite();
    public final Body body = new Body(BodyType.DYNAMIC);

    private final Timer airDashTimer = new Timer(MAX_AIR_DASH_TIME);
    private final Timer dmgTimer = new Timer(DAMAGE_DURATION, true);
    private final Timer chargingTimer = new Timer(TIME_TO_FULLY_CHARGED);
    private final Timer shootAnimTimer = new Timer(SHOOT_ANIM_TIME, true);
    private final Timer groundSlideTimer = new Timer(MAX_GROUND_SLIDE_TIME);
    private final Timer dmgRecovTimer = new Timer(DAMAGE_RECOVERY_TIME, true);
    private final Timer wallJumpTimer = new Timer(WALL_JUMP_IMPETUS_TIME, true);
    private final Timer dmgRecovBlinkTimer = new Timer(DAMAGE_RECOVERY_FLASH_DURATION);

    public boolean shooting;
    public boolean recoveryBlink;
    public MegamanWeapon currentWeapon;
    public AButtonTask aButtonTask = AButtonTask.JUMP;
    @Getter
    @Setter
    public Facing facing = Facing.RIGHT;

    public Megaman(MegamanGame game) {
        super(game, EntityType.MEGAMAN);
        this.weaponHandler = new MegamanWeaponHandler(this, game.getGameEngine(), game.getEntityFactories());
        this.healthHandler = new MegamanHealthHandler(this);
        this.currentWeapon = MegamanWeapon.MEGA_BUSTER;
        addComponent(bodyComponent());
        addComponent(healthComponent());
        addComponent(updatableComponent());
        addComponent(behaviorComponent());
        addComponent(spriteComponent());
        addComponent(animationComponent());
        addComponent(new SoundComponent());
        addComponent(controllerComponent());
        chargingTimer.tmRunnables.add(new TimeMarkedRunnable(TIME_TO_HALFWAY_CHARGED,
                () -> request(SoundAsset.MEGA_BUSTER_CHARGING_SOUND)));
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> spawnData) {
        body.setPos(spawn, Position.BOTTOM_CENTER);
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
    public Vector2 getPos() {
        return ShapeUtils.getBottomCenterPoint(body.bounds);
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

    public void stopCharging() {
        chargingTimer.reset();
        stopLoop(SoundAsset.MEGA_BUSTER_CHARGING_SOUND);
    }

    public boolean isShooting() {
        return !shootAnimTimer.isFinished();
    }

    public boolean shoot() {
        boolean shot = weaponHandler.fireWeapon(currentWeapon, getChargeStatus());
        if (shot) {
            shootAnimTimer.reset();
        }
        return shot;
    }

    public MegaChargeStatus getChargeStatus() {
        if (isChargingFully()) {
            return MegaChargeStatus.FULLY_CHARGED;
        }
        return isCharging() ? MegaChargeStatus.HALF_CHARGED : MegaChargeStatus.NOT_CHARGED;
    }

    public boolean isChargingFully() {
        // TODO: Fix
        // return stats.weaponsChargeable && chargingTimer.isFinished();
        return false;
    }

    public boolean isCharging() {
        // TODO: Fix
        // return stats.weaponsChargeable && chargingTimer.getTime() >= TIME_TO_HALFWAY_CHARGED;
        return false;
    }

    public boolean is(BehaviorType behaviorType) {
        return getComponent(BehaviorComponent.class).is(behaviorType);
    }

    public boolean is(BodySense bodySense) {
        return body.is(bodySense);
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
        // left dpad
        c.ctrlAdapters.put(ControllerBtn.DPAD_LEFT, new ControllerAdapter() {
            @Override
            public void onPressContinued(float delta) {
                if (!dmgTimer.isFinished() || game.getCtrlMan().isPressed(ControllerBtn.DPAD_RIGHT)) {
                    return;
                }
                setFacing(is(BehaviorType.WALL_SLIDING) ? Facing.RIGHT : Facing.LEFT);
                bc.set(BehaviorType.RUNNING, !is(BehaviorType.WALL_SLIDING));
                float threshold = -RUN_SPEED * (body.is(BodySense.IN_WATER) ? .65f : 1f);
                if (body.velocity.x > threshold) {
                    body.velocity.add(50f * delta, 0f);
                }
            }

            @Override
            public void onJustReleased() {
                if (!game.getCtrlMan().isPressed(ControllerBtn.DPAD_RIGHT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }

            @Override
            public void onReleaseContinued() {
                if (!game.getCtrlMan().isPressed(ControllerBtn.DPAD_RIGHT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }
        });
        // right dpad
        c.ctrlAdapters.put(ControllerBtn.DPAD_RIGHT, new ControllerAdapter() {
            @Override
            public void onPressContinued(float delta) {
                if (!dmgTimer.isFinished() || game.getCtrlMan().isPressed(ControllerBtn.DPAD_LEFT)) {
                    return;
                }
                setFacing(is(BehaviorType.WALL_SLIDING) ? Facing.LEFT : Facing.RIGHT);
                bc.set(BehaviorType.RUNNING, !is(BehaviorType.WALL_SLIDING));
                float threshold = RUN_SPEED * (body.is(BodySense.IN_WATER) ? .65f : 1f);
                if (body.velocity.x < threshold) {
                    body.velocity.add(50f * delta, 0f);
                }
            }

            @Override
            public void onJustReleased() {
                if (!game.getCtrlMan().isPressed(ControllerBtn.DPAD_LEFT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }

            @Override
            public void onReleaseContinued() {
                if (!game.getCtrlMan().isPressed(ControllerBtn.DPAD_LEFT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }

        });
        // x
        c.ctrlAdapters.put(ControllerBtn.X, new ControllerAdapter() {
            @Override
            public void onPressContinued(float delta) {
                // TODO: Fix
                /*
                if (!stats.weaponsChargeable) {
                    return;
                }
                 */
                chargingTimer.update(delta);
                if (!dmgTimer.isFinished()) {
                    stopCharging();
                }
            }

            @Override
            public void onJustReleased() {
                if (!shoot()) {
                    // TODO: play error sound
                }
                stopCharging();
            }

        });
        return c;
    }

    private BodyComponent bodyComponent() {
        body.velClamp.set(CLAMP_X, CLAMP_Y);
        body.bounds.width = .8f * WorldConstVals.PPM;
        // model 1
        Rectangle m1 = new Rectangle();
        m1.setSize(.575f * WorldConstVals.PPM, WorldConstVals.PPM / 16f);
        // on bounce runnable
        Runnable onBounce = () -> {
            if (!body.is(BodySense.IN_WATER)) {
                aButtonTask = AButtonTask.AIR_DASH;
            }
        };
        // body
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY, body.bounds);
        body.fixtures.add(bodyFixture);
        // feetFixture
        Fixture feetFixture = new Fixture(this, FixtureType.FEET, m1);
        feetFixture.offset.y = -WorldConstVals.PPM / 2f;
        feetFixture.userData.put(ConstKeys.RUN, onBounce);
        body.fixtures.add(feetFixture);
        // headFixture
        Fixture headFixture = new Fixture(this, FixtureType.HEAD, m1);
        headFixture.offset.y = WorldConstVals.PPM / 2f;
        headFixture.userData.put(ConstKeys.RUN, onBounce);
        body.fixtures.add(headFixture);
        // model 2
        Rectangle m2 = new Rectangle();
        m2.setSize(WorldConstVals.PPM / 16f, WorldConstVals.PPM / 16f);
        // leftFixture side
        Fixture leftFixture = new Fixture(this, FixtureType.SIDE, m2);
        leftFixture.offset.set(-.4f * WorldConstVals.PPM, .15f * WorldConstVals.PPM);
        leftFixture.userData.put(ConstKeys.RUN, onBounce);
        leftFixture.userData.put(ConstKeys.SIDE, ConstKeys.LEFT);
        body.fixtures.add(leftFixture);
        // rightFixture side
        Fixture rightFixture = new Fixture(this, FixtureType.SIDE, m2);
        rightFixture.offset.set(.4f * WorldConstVals.PPM, .15f * WorldConstVals.PPM);
        rightFixture.userData.put(ConstKeys.RUN, onBounce);
        rightFixture.userData.put(ConstKeys.SIDE, ConstKeys.RIGHT);
        body.fixtures.add(rightFixture);
        // hit box, force listener, water listener
        Fixture hitboxFixture = new Fixture(this, FixtureType.DAMAGEABLE, .8f * WorldConstVals.PPM);
        body.fixtures.add(hitboxFixture);
        // body update
        UpdatableComponent u = getComponent(UpdatableComponent.class);
        u.add(delta -> {
            if (is(BehaviorType.GROUND_SLIDING)) {
                body.bounds.height = .45f * WorldConstVals.PPM;
                feetFixture.offset.y = -WorldConstVals.PPM / 4f;
            } else {
                body.bounds.height = .95f * WorldConstVals.PPM;
                feetFixture.offset.y = -WorldConstVals.PPM / 2f;
            }
            if (body.velocity.y < 0f && !body.is(BodySense.FEET_ON_GROUND)) {
                body.gravity.y = UNGROUNDED_GRAVITY;
                if (body.is(BodySense.IN_WATER)) {
                    body.gravity.y /= 2f;
                }
            } else {
                body.gravity.y = GROUNDED_GRAVITY;
                if (body.is(BodySense.IN_WATER)) {
                    body.gravity.y *= 1.75f;
                }
            }
        });
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
        sprite.setSize(1.65f * WorldConstVals.PPM, 1.25f * WorldConstVals.PPM);
        SpriteHandle handle = new SpriteHandle(sprite);
        handle.priority = 3;
        handle.runnable = () -> {
            handle.setPos(body.bounds, Position.BOTTOM_CENTER);
            sprite.setAlpha(recoveryBlink ? 0f : 1f);
            sprite.setFlip(is(BehaviorType.WALL_SLIDING), sprite.isFlipY());
            sprite.translateY(is(BehaviorType.GROUND_SLIDING) ? .1f * WorldConstVals.PPM : 0f);
        };
        return new SpriteComponent(handle);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> {
            if (!dmgTimer.isFinished()) {
                return is(BehaviorType.GROUND_SLIDING) ? "LayDownDamaged" : "Damaged";
            } else if (is(BehaviorType.AIR_DASHING)) {
                if (isChargingFully()) {
                    return "AirDashCharging";
                } else if (isCharging()) {
                    return "AirDashHalfCharging";
                } else {
                    return "AirDash";
                }
            } else if (is(BehaviorType.GROUND_SLIDING)) {
                if (isChargingFully()) {
                    return "GroundSlideCharging";
                } else if (isCharging()) {
                    return "GroundSlideHalfCharging";
                } else {
                    return "GroundSlide";
                }
            } else if (is(BehaviorType.WALL_SLIDING)) {
                if (isShooting()) {
                    return "WallSlideShoot";
                } else if (isChargingFully()) {
                    return "WallSlideCharging";
                } else if (isCharging()) {
                    return "WallSlideHalfCharging";
                } else {
                    return "WallSlide";
                }
            } else if (is(BehaviorType.SWIMMING)) {
                if (isShooting()) {
                    return "SwimShoot";
                } else if (isChargingFully()) {
                    return "SwimCharging";
                } else if (isCharging()) {
                    return "SwimHalfCharging";
                } else {
                    return "Swim";
                }
            } else if (is(BehaviorType.JUMPING) || !is(BodySense.FEET_ON_GROUND)) {
                if (isShooting()) {
                    return "JumpShoot";
                } else if (isChargingFully()) {
                    return "JumpCharging";
                } else if (isCharging()) {
                    return "JumpHalfCharging";
                } else {
                    return "Jump";
                }
            } else if (is(BodySense.FEET_ON_GROUND) && is(BehaviorType.RUNNING)) {
                if (isShooting()) {
                    return "RunShoot";
                } else if (isChargingFully()) {
                    return "RunCharging";
                } else if (isCharging()) {
                    return "RunHalfCharging";
                } else {
                    return "Run";
                }
            } else if (is(BehaviorType.CLIMBING)) {
                if (isShooting()) {
                    return "ClimbShoot";
                } else if (isChargingFully()) {
                    return "ClimbCharging";
                } else if (isCharging()) {
                    return "ClimbHalfCharging";
                } else {
                    return "Climb";
                }
            } else if (is(BodySense.FEET_ON_GROUND) &&
                    Math.abs(body.velocity.x) > WorldConstVals.PPM / 8f) {
                if (isShooting()) {
                    return "SlipSlideShoot";
                } else if (isChargingFully()) {
                    return "SlipSlideCharging";
                } else if (isCharging()) {
                    return "SlipSlideHalfCharging";
                } else {
                    return "SlipSlide";
                }
            } else {
                if (isShooting()) {
                    return "StandShoot";
                } else if (isChargingFully()) {
                    return "StandCharging";
                } else if (isCharging()) {
                    return "StandHalfCharging";
                } else {
                    return "Stand";
                }
            }
        };
        Map<MegamanWeapon, Map<String, Animation>> weaponToAnimMap = new EnumMap<>(MegamanWeapon.class);
        final float chargingAnimTime = .125f;
        for (MegamanWeapon megamanWeapon : MegamanWeapon.values()) {

            // TODO: Temporary, do not include any but mega buster
            if (megamanWeapon != MegamanWeapon.MEGA_BUSTER) {
                continue;
            }

            String textureAtlasKey;
            switch (megamanWeapon) {
                case MEGA_BUSTER -> textureAtlasKey = TextureAsset.MEGAMAN.getSrc();
                case FLAME_TOSS -> textureAtlasKey = TextureAsset.MEGAMAN_FIRE.getSrc();
                default -> throw new IllegalStateException();
            }
            TextureAtlas textureAtlas = game.getAssMan().getAsset(textureAtlasKey, TextureAtlas.class);
            Map<String, Animation> animations = new HashMap<>();
            // climb
            animations.put("Climb", new Animation(textureAtlas.findRegion("Climb"), 2, .125f));
            animations.put("ClimbShoot", new Animation(textureAtlas.findRegion("ClimbShoot")));
            animations.put("ClimbHalfCharging", new Animation(
                    textureAtlas.findRegion("ClimbHalfCharging"), 2, chargingAnimTime));
            animations.put("ClimbCharging", new Animation(
                    textureAtlas.findRegion("ClimbCharging"), 2, chargingAnimTime));
            // stand
            animations.put("Stand", new Animation(textureAtlas.findRegion("Stand"), new float[]{1.5f, .15f}));
            animations.put("StandCharging", new Animation(
                    textureAtlas.findRegion("StandCharging"), 2, chargingAnimTime));
            animations.put("StandHalfCharging", new Animation(
                    textureAtlas.findRegion("StandHalfCharging"), 2, chargingAnimTime));
            animations.put("StandShoot", new Animation(textureAtlas.findRegion("StandShoot")));
            // damaged
            animations.put("Damaged", new Animation(textureAtlas.findRegion("Damaged"), 3, .05f));
            animations.put("LayDownDamaged", new Animation(textureAtlas.findRegion("LayDownDamaged"), 3, .05f));
            // run
            animations.put("Run", new Animation(textureAtlas.findRegion("Run"), 4, .125f));
            animations.put("RunCharging", new Animation(textureAtlas
                    .findRegion("RunCharging"), 4, chargingAnimTime));
            animations.put("RunHalfCharging", new Animation(
                    textureAtlas.findRegion("RunHalfCharging"), 4, chargingAnimTime));
            animations.put("RunShoot", new Animation(textureAtlas.findRegion("RunShoot"), 4, .125f));
            // jump
            animations.put("Jump", new Animation(textureAtlas.findRegion("Jump")));
            animations.put("JumpCharging", new Animation(
                    textureAtlas.findRegion("JumpCharging"), 2, chargingAnimTime));
            animations.put("JumpHalfCharging", new Animation(
                    textureAtlas.findRegion("JumpHalfCharging"), 2, chargingAnimTime));
            animations.put("JumpShoot", new Animation(textureAtlas.findRegion("JumpShoot")));
            // swim
            animations.put("Swim", new Animation(textureAtlas.findRegion("Swim")));
            animations.put("SwimAttack", new Animation(textureAtlas.findRegion("SwimAttack")));
            animations.put("SwimCharging", new Animation(
                    textureAtlas.findRegion("SwimCharging"), 2, chargingAnimTime));
            animations.put("SwimHalfCharging", new Animation(
                    textureAtlas.findRegion("SwimHalfCharging"), 2, chargingAnimTime));
            animations.put("SwimShoot", new Animation(textureAtlas.findRegion("SwimShoot")));
            // wall slide
            animations.put("WallSlide", new Animation(textureAtlas.findRegion("WallSlide")));
            animations.put("WallSlideCharging", new Animation(
                    textureAtlas.findRegion("WallSlideCharging"), 2, chargingAnimTime));
            animations.put("WallSlideHalfCharging", new Animation(
                    textureAtlas.findRegion("WallSlideHalfCharging"), 2, chargingAnimTime));
            animations.put("WallSlideShoot", new Animation(textureAtlas.findRegion("WallSlideShoot")));
            // ground slide
            animations.put("GroundSlide", new Animation(textureAtlas.findRegion("GroundSlide")));
            animations.put("GroundSlideCharging", new Animation(
                    textureAtlas.findRegion("GroundSlideCharging"), 2, chargingAnimTime));
            animations.put("GroundSlideHalfCharging", new Animation(
                    textureAtlas.findRegion("GroundSlideHalfCharging"), 2, chargingAnimTime));
            // air dash
            animations.put("AirDash", new Animation(textureAtlas.findRegion("AirDash")));
            animations.put("AirDashCharging", new Animation(
                    textureAtlas.findRegion("AirDashCharging"), 2, chargingAnimTime));
            animations.put("AirDashHalfCharging", new Animation(
                    textureAtlas.findRegion("AirDashHalfCharging"), 2, chargingAnimTime));
            // slip slide
            animations.put("SlipSlide", new Animation(textureAtlas.findRegion("SlipSlide")));
            animations.put("SlipSlideCharging", new Animation(
                    textureAtlas.findRegion("SlipSlideCharging"), 2, chargingAnimTime));
            animations.put("SlipSlideHalfCharging", new Animation(
                    textureAtlas.findRegion("SlipSlideHalfCharging"), 2, chargingAnimTime));
            animations.put("SlipSlideShoot", new Animation(textureAtlas.findRegion("SlipSlideShoot")));
            weaponToAnimMap.put(megamanWeapon, animations);
        }
        Animator animator = new Animator(sprite, keySupplier, key -> weaponToAnimMap.get(currentWeapon).get(key));
        return new AnimationComponent(animator);
    }

    private BehaviorComponent behaviorComponent() {
        BehaviorComponent c = new BehaviorComponent();
        // wall slide
        Behavior wallSlide = new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                if (!dmgTimer.isFinished() ||
                        c.is(BehaviorType.JUMPING) ||
                        is(BodySense.FEET_ON_GROUND) ||
                        !wallJumpTimer.isFinished() // ||
                    // TODO: Fix
                    // !stats.canUseSpecialAbility(MegamanAbility.WALL_JUMP)
                ) {
                    return false;
                }
                if (is(BodySense.TOUCHING_BLOCK_LEFT) &&
                        game.getCtrlMan().isPressed(ControllerBtn.DPAD_LEFT)) {
                    return true;
                }
                return is(BodySense.TOUCHING_BLOCK_RIGHT) &&
                        game.getCtrlMan().isPressed(ControllerBtn.DPAD_RIGHT);
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
                if (!dmgTimer.isFinished() || !is(BodySense.IN_WATER)) {
                    return false;
                }
                if (c.is(BehaviorType.SWIMMING)) {
                    return body.velocity.y > 0f;
                }
                return game.getCtrlMan().isJustPressed(ControllerBtn.A) &&
                        aButtonTask == AButtonTask.SWIM;
            }

            @Override
            protected void init() {
                float x = 0f;
                float y = 18f;
                if (is(BehaviorType.WALL_SLIDING)) {
                    x = WALL_JUMP_HORIZ * 1.15f;
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
                if (!dmgTimer.isFinished() ||
                        is(BehaviorType.SWIMMING) ||
                        is(BodySense.HEAD_TOUCHING_BLOCK) ||
                        !game.getCtrlMan().isPressed(ControllerBtn.A) ||
                        game.getCtrlMan().isPressed(ControllerBtn.DPAD_DOWN)) {
                    return false;
                }
                return is(BehaviorType.JUMPING) ?
                        body.velocity.y >= 0f :
                        aButtonTask == AButtonTask.JUMP && is(BodySense.FEET_ON_GROUND);
            }

            @Override
            protected void init() {
                c.set(BehaviorType.JUMPING, true);
                boolean wallSliding = is(BehaviorType.WALL_SLIDING);
                Vector2 vel = new Vector2();
                float x;
                if (wallSliding) {
                    x = WALL_JUMP_HORIZ;
                    if (is(Facing.LEFT)) {
                        x *= -1f;
                    }
                } else {
                    x = body.velocity.x;
                }
                float y = wallSliding ? WALL_JUMP_VEL : JUMP_VEL;
                vel.set(x, y);
                body.velocity.set(vel);
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
                if (!dmgTimer.isFinished() ||
                        airDashTimer.isFinished() ||
                        is(BehaviorType.WALL_SLIDING) ||
                        is(BodySense.FEET_ON_GROUND) // ||
                    // TODO: fix
                    // !stats.canUseSpecialAbility(MegamanAbility.AIR_DASH)
                ) {
                    return false;
                }
                return is(BehaviorType.AIR_DASHING) ?
                        game.getCtrlMan().isPressed(ControllerBtn.A) :
                        game.getCtrlMan().isJustPressed(ControllerBtn.A) && aButtonTask == AButtonTask.AIR_DASH;
            }

            @Override
            protected void init() {
                request(SoundAsset.WHOOSH_SOUND);
                body.gravityOn = false;
                aButtonTask = AButtonTask.JUMP;
                c.set(BehaviorType.AIR_DASHING, false);
            }

            @Override
            protected void act(float delta) {
                airDashTimer.update(delta);
                body.velocity.y = 0f;
                if ((is(Facing.LEFT) && is(BodySense.TOUCHING_BLOCK_LEFT)) ||
                        (is(Facing.RIGHT) && is(BodySense.TOUCHING_BLOCK_RIGHT))) {
                    return;
                }
                float x = AIR_DASH_VEL;
                if (is(BodySense.IN_WATER)) {
                    x /= 2f;
                }
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
                float x = AIR_DASH_END_BUMP;
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
                if (is(BehaviorType.GROUND_SLIDING) &&
                        is(BodySense.HEAD_TOUCHING_BLOCK)) {
                    return true;
                }
                if (!dmgTimer.isFinished() ||
                        groundSlideTimer.isFinished() ||
                        !is(BodySense.FEET_ON_GROUND)) {
                    return false;
                }
                if (!game.getCtrlMan().isPressed(ControllerBtn.DPAD_DOWN)) {
                    return false;
                }
                return is(BehaviorType.GROUND_SLIDING) ?
                        game.getCtrlMan().isPressed(ControllerBtn.A) :
                        game.getCtrlMan().isJustPressed(ControllerBtn.A);
            }

            @Override
            protected void init() {
                c.set(BehaviorType.GROUND_SLIDING, true);
            }

            @Override
            protected void act(float delta) {
                groundSlideTimer.update(delta);
                if (!dmgTimer.isFinished() ||
                        (is(Facing.LEFT) && is(BodySense.TOUCHING_BLOCK_LEFT)) ||
                        (is(Facing.RIGHT) && is(BodySense.TOUCHING_BLOCK_RIGHT))) {
                    return;
                }
                float x = GROUND_SLIDE_VEL * (is(BodySense.IN_WATER) ? .5f : 1f);
                if (is(Facing.LEFT)) {
                    x *= -1f;
                }
                body.velocity.x = x;
            }

            @Override
            protected void end() {
                groundSlideTimer.reset();
                c.set(BehaviorType.GROUND_SLIDING, false);
                float endDash = is(BodySense.IN_WATER) ? 2f : 5f;
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
        UpdatableComponent c = new UpdatableComponent();
        c.add(delta -> {
            // timers
            // TODO: Fix
            /*
            if (!stats.weaponsChargeable) {
                chargingTimer.reset();
            }
             */
            dmgTimer.update(delta);
            if (!dmgTimer.isFinished()) {
                chargingTimer.reset();
                stopLoop(SoundAsset.MEGA_BUSTER_CHARGING_SOUND);
                body.velocity.add((is(Facing.LEFT) ? .15f : -.15f) * WorldConstVals.PPM, 0f);
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
            // TODO: weapon defs
            // megamanWeaponDefs.get(currentWeapon).updateCooldownTimer(delta);
        });
        return c;
    }


}
