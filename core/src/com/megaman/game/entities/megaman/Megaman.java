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
import com.megaman.game.entities.megaman.animations.MegamanAnimator;
import com.megaman.game.entities.megaman.events.MegamanDeathEvent;
import com.megaman.game.entities.megaman.health.MegaHealthTank;
import com.megaman.game.entities.megaman.health.MegamanHealthTankHandler;
import com.megaman.game.entities.megaman.upgrades.MegamanAbility;
import com.megaman.game.entities.megaman.upgrades.MegamanUpgradeHandler;
import com.megaman.game.entities.megaman.vals.AButtonTask;
import com.megaman.game.entities.megaman.vals.MegamanDamageNegs;
import com.megaman.game.entities.megaman.weapons.MegamanWeapon;
import com.megaman.game.entities.megaman.weapons.MegamanWeaponHandler;
import com.megaman.game.entities.projectiles.ChargeStatus;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.health.HealthComponent;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Positional;
import com.megaman.game.utils.objs.KeyValuePair;
import com.megaman.game.utils.objs.TimeMarkedRunnable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

public class Megaman extends Entity implements Damageable, Faceable, Positional, EventListener {

    public static final int START_MAX_HEALTH = 14;

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

    public static final float SHOOT_ANIM_TIME = .5f;
    public static final float CHARGING_ANIM_TIME = .125f;

    public final MegamanWeaponHandler weaponHandler;
    public final MegamanUpgradeHandler upgradeHandler;
    public final MegamanHealthTankHandler healthTankHandler;

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

    @Getter
    @Setter
    public int maxHealth;

    public MegamanWeapon currWeapon;
    public AButtonTask aButtonTask;

    @Getter
    @Setter
    public Facing facing;

    private boolean recoveryBlink;

    @Getter
    @Setter
    private boolean ready;

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

        // TODO: for now make all weapons available
        weaponHandler.putWeapon(MegamanWeapon.MEGA_BUSTER);
        weaponHandler.putWeapon(MegamanWeapon.FLAME_TOSS);

        upgradeHandler = new MegamanUpgradeHandler(this);
        // TODO: for now add all abilities
        upgradeHandler.add(MegamanAbility.WALL_JUMP);
        upgradeHandler.add(MegamanAbility.AIR_DASH);
        upgradeHandler.add(MegamanAbility.GROUND_SLIDE);

        healthTankHandler = new MegamanHealthTankHandler(this);
        maxHealth = START_MAX_HEALTH;
        putComponent(updatableComponent());
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(behaviorComponent());
        putComponent(controllerComponent());
        putComponent(new SoundComponent());
        putComponent(new HealthComponent(this::getMaxHealth));
        putComponent(new AnimationComponent(MegamanAnimator.getAnimator(this)));
        runOnDeath.add(new MegamanDeathEvent(this));
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> spawnData) {
        body.velocity.setZero();
        body.bounds.setPosition(spawn);
        facing = Facing.RIGHT;
        aButtonTask = AButtonTask.JUMP;
        currWeapon = MegamanWeapon.MEGA_BUSTER;
        weaponHandler.reset();
        dmgRecovBlinkTimer.reset();
        shootAnimTimer.setToEnd();
        groundSlideTimer.reset();
        dmgRecovTimer.setToEnd();
        wallJumpTimer.setToEnd();
        chargingTimer.reset();
        airDashTimer.reset();
        dmgTimer.setToEnd();
        game.getEventMan().add(this);
    }

    @Override
    public void listenForEvent(Event e) {
        switch (e.type) {
            case BEGIN_ROOM_TRANS, CONTINUE_ROOM_TRANS -> {
                body.velocity.set(Vector2.Zero);
                Vector2 pos = e.getInfo(ConstKeys.POS, Vector2.class);
                body.setPos(pos, Position.BOTTOM_CENTER);
                request(SoundAsset.MEGA_BUSTER_CHARGING_SOUND, false);
            }
            case GATE_INIT_OPENING -> {
                body.velocity.setZero();
                request(SoundAsset.MEGA_BUSTER_CHARGING_SOUND, false);
            }
        }
    }

    @Override
    public Set<Class<? extends Damager>> getDamagerMaskSet() {
        return MegamanDamageNegs.getDamagerMaskSet();
    }

    @Override
    public void takeDamageFrom(Damager damager) {
        DamageNegotiation dmgNeg = MegamanDamageNegs.get(damager);
        dmgTimer.reset();
        dmgNeg.runOnDamage();
        removeHealth(dmgNeg.getDamage(damager));
        request(SoundAsset.MEGAMAN_DAMAGE_SOUND, true);
        request(SoundAsset.MEGA_BUSTER_CHARGING_SOUND, false);
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

    public int getHealth() {
        return getComponent(HealthComponent.class).getHealth();
    }

    public void addHealth(int health) {
        getComponent(HealthComponent.class).translateHealth(Math.abs(health));
    }

    public boolean addHealthToTanks(int health) {
        return healthTankHandler.add(health);
    }

    public void removeHealth(int health) {
        getComponent(HealthComponent.class).translateHealth(-Math.abs(health));
    }

    public boolean has(MegaHealthTank tank) {
        return healthTankHandler.has(tank);
    }

    public void put(MegaHealthTank tank) {
        put(tank, getMaxHealth());
    }

    public void put(MegaHealthTank tank, int health) {
        healthTankHandler.put(tank, health);
    }

    public int getAmmo() {
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

    public boolean has(MegamanAbility ability) {
        return upgradeHandler.has(ability);
    }

    public void add(MegamanAbility ability) {
        upgradeHandler.add(ability);
    }

    public void remove(MegamanAbility ability) {
        upgradeHandler.remove(ability);
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
        Fixture playerFixture = new Fixture(this, FixtureType.PLAYER,
                new Rectangle().setWidth(.8f * WorldVals.PPM));
        body.fixtures.add(playerFixture);
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
            ((Rectangle) playerFixture.shape).set(body.bounds);
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
            handle.hidden = !ready;
            handle.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setAlpha(isInvincible() ? (recoveryBlink ? 0f : 1f) : 1f);
            sprite.translateY(is(BehaviorType.GROUND_SLIDING) ? -.1f * WorldVals.PPM : 0f);
            sprite.setFlip(is(BehaviorType.WALL_SLIDING) ? is(Facing.RIGHT) : is(Facing.LEFT), sprite.isFlipY());
        };
        return new SpriteComponent(handle);
    }

    private BehaviorComponent behaviorComponent() {
        BehaviorComponent c = new BehaviorComponent();
        ControllerManager ctrlMan = game.getCtrlMan();
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
        c.add(wallSlide);
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
        c.add(swim);
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
        c.add(jump);
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
        c.add(airDash);
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
        c.add(groundSlide);
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
