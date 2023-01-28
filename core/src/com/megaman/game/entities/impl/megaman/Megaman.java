package com.megaman.game.entities.impl.megaman;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.behaviors.Behavior;
import com.megaman.game.behaviors.BehaviorComponent;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.controllers.ControllerActuator;
import com.megaman.game.controllers.ControllerComponent;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.controllers.CtrlBtn;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.utils.damage.DamageNegotiation;
import com.megaman.game.entities.utils.damage.Damageable;
import com.megaman.game.entities.utils.damage.Damager;
import com.megaman.game.entities.utils.faceable.Faceable;
import com.megaman.game.entities.utils.faceable.Facing;
import com.megaman.game.entities.impl.enemies.impl.SpringHead;
import com.megaman.game.entities.impl.megaman.animations.MegamanAnimator;
import com.megaman.game.entities.impl.megaman.events.MegamanDeathEvent;
import com.megaman.game.entities.impl.megaman.upgrades.*;
import com.megaman.game.entities.impl.megaman.vals.AButtonTask;
import com.megaman.game.entities.impl.megaman.vals.MegamanDamageNegs;
import com.megaman.game.entities.impl.megaman.weapons.MegamanWeapon;
import com.megaman.game.entities.impl.megaman.weapons.MegamanWeaponHandler;
import com.megaman.game.entities.impl.projectiles.ChargeStatus;
import com.megaman.game.entities.impl.special.SpecialFactory;
import com.megaman.game.entities.impl.special.impl.Ladder;
import com.megaman.game.entities.utils.special.UpsideDownable;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.health.HealthComponent;
import com.megaman.game.screens.levels.camera.CamFocusable;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Positional;
import com.megaman.game.utils.objs.TimeMarkedRunnable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

public class Megaman extends Entity implements Damageable, Faceable, Positional, CamFocusable,
        EventListener, UpsideDownable {

    private static final Logger logger = new Logger(Megaman.class, MegamanGame.DEBUG && true);

    public static final int START_MAX_HEALTH = 14;

    public static final float CLAMP_X = 15f;
    public static final float CLAMP_Y = 25f;

    public static final float RUN_SPEED = 5f;
    public static final float RUN_IMPULSE = 50f;
    public static final float ICE_RUN_IMPULSE = 15f;
    public static final float WATER_RUN_SPEED = 2.25f;

    public static final float SWIM_VEL_Y = 20f;

    public static final float JUMP_VEL = 24f;
    public static final float WATER_JUMP_VEL = 28f;
    public static final float WATER_WALL_JUMP_VEL = 38f;
    public static final float WALL_JUMP_VEL = 42f;

    public static final float WALL_JUMP_HORIZ = 10f;
    public static final float WALL_JUMP_IMPETUS_TIME = .1f;

    public static final float GROUND_GRAVITY = -.0015f;
    public static final float GRAVITY = -.375f;
    public static final float ICE_GRAVITY = -.5f;
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

    public static final float CLIMB_VEL = 2.5f;

    public static final float DAMAGE_DURATION = .75f;
    public static final float DAMAGE_RECOVERY_TIME = 1.5f;
    public static final float DAMAGE_RECOVERY_FLASH_DURATION = .05f;

    public static final float TIME_TO_HALFWAY_CHARGED = .5f;
    public static final float TIME_TO_FULLY_CHARGED = 1.25f;

    public static final float SHOOT_ANIM_TIME = .3f;

    private static final float DMG_X = 8f;
    private static final float DMG_Y = 5f;

    public final Sprite sprite;
    public final Body body;

    public final MegamanWeaponHandler weaponHandler;
    public final MegaUpgradeHandler upgradeHandler;

    private final Timer dmgTimer;
    private final Timer airDashTimer;
    private final Timer dmgRecovTimer;
    private final Timer chargingTimer;
    private final Timer wallJumpTimer;
    private final Timer shootAnimTimer;
    private final Timer groundSlideTimer;
    private final Timer dmgRecovBlinkTimer;

    private final ObjectSet<Class<? extends Damager>> noDmgBounce;

    public MegamanWeapon currWeapon;
    public AButtonTask aButtonTask;

    private float jumpVel;
    private float wallJumpVel;
    private float waterJumpVel;
    private float waterWallJumpVel;

    private float gravity;
    private float groundGravity;
    private float iceGravity;
    private float waterGravity;
    private float waterIceGravity;
    private float swimVelY;

    @Getter
    private boolean upsideDown;

    @Getter
    @Setter
    public int maxHealth;
    @Getter
    @Setter
    public Facing facing;
    @Getter
    @Setter
    public boolean maverick;
    @Getter
    @Setter
    private boolean ready;
    @Getter
    @Setter
    private boolean damageable;
    private boolean recoveryBlink;

    public Megaman(MegamanGame game) {
        super(game, EntityType.MEGAMAN);
        sprite = new Sprite();
        body = new Body(BodyType.DYNAMIC, true);

        // y vals
        setUpsideDown(false);

        // timers
        airDashTimer = new Timer(MAX_AIR_DASH_TIME);
        dmgTimer = new Timer(DAMAGE_DURATION, true);
        shootAnimTimer = new Timer(SHOOT_ANIM_TIME, true);
        groundSlideTimer = new Timer(MAX_GROUND_SLIDE_TIME);
        dmgRecovTimer = new Timer(DAMAGE_RECOVERY_TIME, true);
        wallJumpTimer = new Timer(WALL_JUMP_IMPETUS_TIME, true);
        dmgRecovBlinkTimer = new Timer(DAMAGE_RECOVERY_FLASH_DURATION);
        chargingTimer = new Timer(TIME_TO_FULLY_CHARGED, new TimeMarkedRunnable(TIME_TO_HALFWAY_CHARGED,
                () -> request(SoundAsset.MEGA_BUSTER_CHARGING_SOUND, true)));

        // no dmg bounce
        noDmgBounce = new ObjectSet<>() {{
            add(SpringHead.class);
        }};

        // weapons
        currWeapon = MegamanWeapon.MEGA_BUSTER;
        weaponHandler = new MegamanWeaponHandler(this);

        // TODO: for now make all weapons available
        weaponHandler.putWeapon(MegamanWeapon.MEGA_BUSTER);
        weaponHandler.putWeapon(MegamanWeapon.FLAME_TOSS);


        // upgrades
        upgradeHandler = new MegaUpgradeHandler(this);
        // TODO: for now add all abilities
        upgradeHandler.add(MegaAbility.WALL_JUMP);
        upgradeHandler.add(MegaAbility.AIR_DASH);
        upgradeHandler.add(MegaAbility.GROUND_SLIDE);

        maxHealth = START_MAX_HEALTH;
        putComponent(updatableComponent());
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(behaviorComponent());
        putComponent(controllerComponent());
        putComponent(new SoundComponent());
        putComponent(new HealthComponent(this::getMaxHealth));
        putComponent(new AnimationComponent(new MegamanAnimator(this)));
        runOnDeath.add(new MegamanDeathEvent(this));
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        // body
        body.labels.add(BodyLabel.PLAYER_BODY);
        body.velocity.setZero();
        body.bounds.setPosition(spawn);

        // vals
        setUpsideDown(false);
        facing = Facing.RIGHT;
        aButtonTask = AButtonTask.JUMP;
        currWeapon = MegamanWeapon.MEGA_BUSTER;

        // weapon
        weaponHandler.reset();

        // timers
        dmgRecovBlinkTimer.reset();
        shootAnimTimer.setToEnd();
        groundSlideTimer.reset();
        dmgRecovTimer.setToEnd();
        wallJumpTimer.setToEnd();
        chargingTimer.reset();
        airDashTimer.reset();
        dmgTimer.setToEnd();

        // damageable
        damageable = !data.containsKey(ConstKeys.DAMAGEABLE) || (boolean) data.get(ConstKeys.DAMAGEABLE);

        // add this as event listener
        game.getEventMan().add(this);
    }

    @Override
    public void setUpsideDown(boolean upsideDown) {
        this.upsideDown = upsideDown;
        if (upsideDown) {
            // jump
            jumpVel = -JUMP_VEL;
            wallJumpVel = -WALL_JUMP_VEL;
            waterJumpVel = -WATER_JUMP_VEL;
            waterWallJumpVel = -WATER_WALL_JUMP_VEL;

            // gravity
            gravity = -GRAVITY;
            groundGravity = -GROUND_GRAVITY;
            iceGravity = -ICE_GRAVITY;
            waterGravity = -WATER_GRAVITY;
            waterIceGravity = -WATER_ICE_GRAVITY;

            // swim
            swimVelY = -SWIM_VEL_Y;
        } else {
            // jump
            jumpVel = JUMP_VEL;
            wallJumpVel = WALL_JUMP_VEL;
            waterJumpVel = WATER_JUMP_VEL;
            waterWallJumpVel = WATER_WALL_JUMP_VEL;

            // gravity
            gravity = GRAVITY;
            groundGravity = GROUND_GRAVITY;
            iceGravity = ICE_GRAVITY;
            waterGravity = WATER_GRAVITY;
            waterIceGravity = WATER_ICE_GRAVITY;

            // swim
            swimVelY = SWIM_VEL_Y;
        }
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
    public Vector2 getFocus() {
        return getPosition();
    }

    @Override
    public Vector2 getTransPoint() {
        Position p = is(Facing.LEFT) ? Position.CENTER_LEFT : Position.CENTER_RIGHT;
        return ShapeUtils.getPoint(body.bounds, p);
    }

    @Override
    public void listenForEvent(Event e) {
        switch (e.type) {
            case BEGIN_ROOM_TRANS, CONTINUE_ROOM_TRANS -> {

                // TODO: set vel zero?
                // body.velocity.set(Vector2.Zero);

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
        if (!noDmgBounce.contains(damager.getClass()) && damager instanceof Entity e &&
                e.hasComponent(BodyComponent.class)) {
            Body enemyBody = e.getComponent(BodyComponent.class).body;
            body.velocity.x = (enemyBody.isRightOf(body) ? -DMG_X : DMG_X) * WorldVals.PPM;
            body.velocity.y = DMG_Y * WorldVals.PPM;
        }
        DamageNegotiation dmgNeg = MegamanDamageNegs.get(damager);
        dmgTimer.reset();
        dmgNeg.runOnDamage();
        removeHealth(dmgNeg.getDamage(damager));
        request(SoundAsset.MEGAMAN_DAMAGE_SOUND, true);
        request(SoundAsset.MEGA_BUSTER_CHARGING_SOUND, false);
    }

    @Override
    public boolean isInvincible() {
        return !damageable || isDamaged() || !dmgRecovTimer.isFinished();
    }

    public boolean isDamaged() {
        return !dmgTimer.isFinished();
    }

    public int getHealth() {
        return getComponent(HealthComponent.class).getHealth();
    }

    public void addHealth(int health) {
        getComponent(HealthComponent.class).translateHealth(Math.abs(health));
    }

    public void removeHealth(int health) {
        getComponent(HealthComponent.class).translateHealth(-Math.abs(health));
    }

    public boolean addHealthToTanks(int health) {
        return upgradeHandler.add(health);
    }

    public void put(MegaHealthTank tank) {
        put(tank, 0);
    }

    public void put(MegaHealthTank tank, int health) {
        upgradeHandler.put(tank, health);
    }

    public void add(MegaHeartTank heartTank) {
        upgradeHandler.add(heartTank);
    }

    public void add(MegaArmorPiece armorPiece) {
        upgradeHandler.add(armorPiece);
    }

    public boolean has(MegaHealthTank tank) {
        return upgradeHandler.has(tank);
    }

    public boolean has(MegaHeartTank heartTank) {
        return upgradeHandler.has(heartTank);
    }

    public boolean has(MegaArmorPiece armorPiece) {
        return upgradeHandler.has(armorPiece);
    }

    public int getAmmo() {
        if (currWeapon == MegamanWeapon.MEGA_BUSTER) {
            return Integer.MAX_VALUE;
        }
        return weaponHandler.getAmmo(currWeapon);
    }

    public void stopCharging() {
        request(SoundAsset.MEGA_BUSTER_CHARGING_SOUND, false);
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

    public boolean has(MegaAbility ability) {
        return upgradeHandler.has(ability);
    }

    public void add(MegaAbility ability) {
        upgradeHandler.add(ability);
    }

    public boolean is(BehaviorType behaviorType) {
        return getComponent(BehaviorComponent.class).is(behaviorType);
    }

    public boolean isAny(BehaviorType... behaviorTypes) {
        return getComponent(BehaviorComponent.class).isAny(behaviorTypes);
    }

    public boolean isAll(BehaviorType... behaviorTypes) {
        return getComponent(BehaviorComponent.class).isAll(behaviorTypes);
    }

    public boolean is(BodySense bodySense) {
        return body.is(bodySense);
    }

    public boolean isAny(BodySense... bodySenses) {
        return body.isAny(bodySenses);
    }

    public boolean isAll(BodySense... bodySenses) {
        return body.isAll(bodySenses);
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
        c.ctrlAdapters.put(CtrlBtn.DPAD_LEFT, new ControllerActuator() {
            @Override
            public void onPressContinued(float delta) {
                if (isDamaged() || ctrlMan.isPressed(CtrlBtn.DPAD_RIGHT)) {
                    return;
                }
                setFacing(is(BehaviorType.WALL_SLIDING) ? Facing.RIGHT : Facing.LEFT);
                if (is(BehaviorType.CLIMBING)) {
                    return;
                }
                bc.set(BehaviorType.RUNNING, !is(BehaviorType.WALL_SLIDING));
                float threshold = (body.is(BodySense.BODY_IN_WATER) ? WATER_RUN_SPEED : RUN_SPEED) * WorldVals.PPM;
                if (body.velocity.x > -threshold) {
                    float impulseX = is(BodySense.FEET_ON_ICE) ? ICE_RUN_IMPULSE : RUN_IMPULSE;
                    body.velocity.x -= impulseX * delta * WorldVals.PPM;
                }
            }

            @Override
            public void onJustReleased() {
                if (!ctrlMan.isPressed(CtrlBtn.DPAD_RIGHT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }

            @Override
            public void onReleaseContinued() {
                if (!ctrlMan.isPressed(CtrlBtn.DPAD_RIGHT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }
        });
        c.ctrlAdapters.put(CtrlBtn.DPAD_RIGHT, new ControllerActuator() {
            @Override
            public void onPressContinued(float delta) {
                if (isDamaged() || ctrlMan.isPressed(CtrlBtn.DPAD_LEFT)) {
                    return;
                }
                setFacing(is(BehaviorType.WALL_SLIDING) ? Facing.LEFT : Facing.RIGHT);
                if (is(BehaviorType.CLIMBING)) {
                    return;
                }
                bc.set(BehaviorType.RUNNING, !is(BehaviorType.WALL_SLIDING));
                float threshold = (body.is(BodySense.BODY_IN_WATER) ? WATER_RUN_SPEED : RUN_SPEED) * WorldVals.PPM;
                if (body.velocity.x < threshold) {
                    float impulseX = is(BodySense.FEET_ON_ICE) ? ICE_RUN_IMPULSE : RUN_IMPULSE;
                    body.velocity.x += impulseX * delta * WorldVals.PPM;
                }
            }

            @Override
            public void onJustReleased() {
                if (!ctrlMan.isPressed(CtrlBtn.DPAD_LEFT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }

            @Override
            public void onReleaseContinued() {
                if (!ctrlMan.isPressed(CtrlBtn.DPAD_LEFT)) {
                    bc.set(BehaviorType.RUNNING, false);
                }
            }
        });
        c.ctrlAdapters.put(CtrlBtn.X, new ControllerActuator() {
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
                if (isDamaged() || !canFireCurrWeapon() || !shoot()) {
                    getComponent(SoundComponent.class).requestToPlay(SoundAsset.ERROR_SOUND);
                }
                stopCharging();
            }

            @Override
            public void onReleaseContinued() {
                stopCharging();
            }
        });
        c.ctrlAdapters.put(CtrlBtn.SELECT, new ControllerActuator() {
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
        body.velClamp.set(CLAMP_X * WorldVals.PPM, CLAMP_Y * WorldVals.PPM);
        body.bounds.width = .75f * WorldVals.PPM;
        body.affectedByResistance = true;

        Array<ShapeHandle> h = new Array<>();

        // player fixture
        Fixture playerFixture = new Fixture(this, FixtureType.PLAYER,
                new Rectangle().setWidth(.8f * WorldVals.PPM));
        body.add(playerFixture);

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY,
                new Rectangle().setWidth(.8f * WorldVals.PPM));
        body.add(bodyFixture);
        h.add(new ShapeHandle(bodyFixture.shape, Color.YELLOW));

        Runnable onBounce = () -> {
            if (!body.is(BodySense.BODY_IN_WATER) && has(MegaAbility.AIR_DASH)) {
                aButtonTask = AButtonTask.AIR_DASH;
            }
        };

        // feet fixture
        Fixture feetFixture = new Fixture(this, FixtureType.FEET,
                new Rectangle().setSize(.6f * WorldVals.PPM, .15f * WorldVals.PPM));
        feetFixture.putUserData(ConstKeys.RUN, onBounce);
        body.add(feetFixture);
        h.add(new ShapeHandle(feetFixture.shape, Color.GREEN));

        // head fixture
        Fixture headFixture = new Fixture(this, FixtureType.HEAD,
                new Rectangle().setSize(.6f * WorldVals.PPM, .2f * WorldVals.PPM));
        headFixture.putUserData(ConstKeys.RUN, onBounce);
        body.add(headFixture);
        h.add(new ShapeHandle(headFixture.shape, Color.ORANGE));

        // left fixture
        Fixture leftFixture = new Fixture(this, FixtureType.SIDE,
                new Rectangle().setWidth(.2f * WorldVals.PPM));
        leftFixture.offset.x = -.4f * WorldVals.PPM;
        leftFixture.putUserData(ConstKeys.RUN, onBounce);
        leftFixture.putUserData(ConstKeys.SIDE, ConstKeys.LEFT);
        body.add(leftFixture);
        h.add(new ShapeHandle(leftFixture.shape, Color.PINK));

        // right fixture
        Fixture rightFixture = new Fixture(this, FixtureType.SIDE,
                new Rectangle().setWidth(.2f * WorldVals.PPM));
        rightFixture.offset.x = .4f * WorldVals.PPM;
        rightFixture.putUserData(ConstKeys.RUN, onBounce);
        rightFixture.putUserData(ConstKeys.SIDE, ConstKeys.RIGHT);
        body.add(rightFixture);
        h.add(new ShapeHandle(rightFixture.shape, Color.PINK));

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(.8f * WorldVals.PPM));
        body.add(damageableFixture);
        h.add(new ShapeHandle(damageableFixture.shape, Color.RED));

        // water listener fixture
        Fixture waterListenerFixture = new Fixture(this, FixtureType.WATER_LISTENER,
                new Rectangle().setSize(.8f * WorldVals.PPM, WorldVals.PPM / 4f));
        h.add(new ShapeHandle(waterListenerFixture.shape, Color.BLUE));
        body.add(waterListenerFixture);

        // pre-process
        body.preProcess = delta -> {
            headFixture.offset.y = (isUpsideDown() ? -.45f : .45f) * WorldVals.PPM;
            if (is(BehaviorType.GROUND_SLIDING)) {
                body.bounds.height = .45f * WorldVals.PPM;
                feetFixture.offset.y = (isUpsideDown() ? .25f : -.25f) * WorldVals.PPM;
                ((Rectangle) leftFixture.shape).setHeight(.2f * WorldVals.PPM);
                ((Rectangle) rightFixture.shape).setHeight(.2f * WorldVals.PPM);
            } else {
                body.bounds.height = .95f * WorldVals.PPM;
                feetFixture.offset.y = (isUpsideDown() ? .45f : -.45f) * WorldVals.PPM;
                ((Rectangle) leftFixture.shape).setHeight(.5f * WorldVals.PPM);
                ((Rectangle) rightFixture.shape).setHeight(.5f * WorldVals.PPM);
            }
            ((Rectangle) bodyFixture.shape).set(body.bounds);
            ((Rectangle) playerFixture.shape).set(body.bounds);
            boolean wallSlidingOnIce = is(BehaviorType.WALL_SLIDING) &&
                    (is(BodySense.SIDE_TOUCHING_ICE_LEFT) || is(BodySense.SIDE_TOUCHING_ICE_RIGHT));
            float gravityY;
            if (is(BodySense.BODY_IN_WATER)) {
                gravityY = wallSlidingOnIce ? waterIceGravity : waterGravity;
            } else if (wallSlidingOnIce) {
                gravityY = iceGravity;
            } else {
                gravityY = is(BodySense.FEET_ON_GROUND) ? groundGravity : gravity;
            }
            body.gravity.y = gravityY * WorldVals.PPM;
        };

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }

        return new BodyComponent(body);
    }

    private BehaviorComponent behaviorComponent() {
        BehaviorComponent c = new BehaviorComponent();
        ControllerManager ctrlMan = game.getCtrlMan();

        // wall slide
        c.add(new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                if (isDamaged() || isAny(BehaviorType.JUMPING, BehaviorType.CLIMBING) || is(BodySense.FEET_ON_GROUND) ||
                        !wallJumpTimer.isFinished() || !upgradeHandler.has(MegaAbility.WALL_JUMP)) {
                    return false;
                }
                if (is(BodySense.SIDE_TOUCHING_BLOCK_LEFT) && ctrlMan.isPressed(CtrlBtn.DPAD_LEFT)) {
                    return true;
                }
                return is(BodySense.SIDE_TOUCHING_BLOCK_RIGHT) && ctrlMan.isPressed(CtrlBtn.DPAD_RIGHT);
            }

            @Override
            protected void init() {
                c.set(BehaviorType.WALL_SLIDING, true);
                aButtonTask = AButtonTask.JUMP; // is(BodySense.BODY_IN_WATER) ? AButtonTask.SWIM : AButtonTask.JUMP;
            }

            @Override
            protected void act(float delta) {
                body.resistance.y += 1.25f;
            }

            @Override
            protected void end() {
                c.set(BehaviorType.WALL_SLIDING, false);
                if (!is(BodySense.BODY_IN_WATER)) {
                    aButtonTask = AButtonTask.AIR_DASH;
                }
            }
        });

        // swim
        c.add(new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                if (isDamaged() || !is(BodySense.BODY_IN_WATER) || is(BodySense.HEAD_TOUCHING_BLOCK)) {
                    return false;
                }
                if (is(BehaviorType.SWIMMING)) {
                    return body.velocity.y > 0f;
                }
                return ctrlMan.isJustPressed(CtrlBtn.A) && aButtonTask == AButtonTask.SWIM;
            }

            @Override
            protected void init() {
                body.velocity.y += swimVelY * WorldVals.PPM;
                if (is(BehaviorType.SWIMMING)) {
                    float x = WALL_JUMP_HORIZ * WorldVals.PPM;
                    if (is(Facing.LEFT)) {
                        x *= -1f;
                    }
                    body.velocity.x = x;
                }
                c.set(BehaviorType.SWIMMING, true);
                game.getAudioMan().playMusic(SoundAsset.SWIM_SOUND);
            }

            @Override
            protected void act(float delta) {
            }

            @Override
            protected void end() {
                c.set(BehaviorType.SWIMMING, false);
            }
        });

        // jump
        c.add(new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                if (isDamaged() || isAny(BehaviorType.SWIMMING, BehaviorType.CLIMBING) ||
                        is(BodySense.HEAD_TOUCHING_BLOCK) || !ctrlMan.isPressed(CtrlBtn.A) ||
                        ctrlMan.isPressed(isUpsideDown() ? CtrlBtn.DPAD_UP : CtrlBtn.DPAD_DOWN)) {
                    return false;
                }
                return is(BehaviorType.JUMPING) ?
                        (isUpsideDown() ? body.velocity.y <= 0f : body.velocity.y >= 0f) :
                        aButtonTask == AButtonTask.JUMP && ctrlMan.isJustPressed(CtrlBtn.A) &&
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
                if (is(BodySense.BODY_IN_WATER)) {
                    v.y = (is(BehaviorType.WALL_SLIDING) ? waterWallJumpVel : waterJumpVel) * WorldVals.PPM;
                } else {
                    v.y = (is(BehaviorType.WALL_SLIDING) ? wallJumpVel : jumpVel) * WorldVals.PPM;
                }
                body.velocity.set(v);
                if (is(BehaviorType.WALL_SLIDING)) {
                    request(SoundAsset.WALL_JUMP, true);
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
        });

        // air dash
        c.add(new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                if (isDamaged() || airDashTimer.isFinished() || is(BodySense.FEET_ON_GROUND) ||
                        isAny(BehaviorType.WALL_SLIDING, BehaviorType.CLIMBING) ||
                        !upgradeHandler.has(MegaAbility.AIR_DASH)) {
                    return false;
                }
                return is(BehaviorType.AIR_DASHING) ? ctrlMan.isPressed(CtrlBtn.A) :
                        ctrlMan.isJustPressed(CtrlBtn.A) && aButtonTask == AButtonTask.AIR_DASH;
            }

            @Override
            protected void init() {
                body.gravityOn = false;
                aButtonTask = AButtonTask.JUMP;
                request(SoundAsset.WHOOSH_SOUND, true);
                c.set(BehaviorType.AIR_DASHING, true);
            }

            @Override
            protected void act(float delta) {
                airDashTimer.update(delta);
                body.velocity.y = 0f;
                if ((is(Facing.LEFT) && is(BodySense.SIDE_TOUCHING_BLOCK_LEFT)) ||
                        (is(Facing.RIGHT) && is(BodySense.SIDE_TOUCHING_BLOCK_RIGHT))) {
                    return;
                }
                float x = (is(BodySense.BODY_IN_WATER) ? WATER_AIR_DASH_VEL : AIR_DASH_VEL) * WorldVals.PPM;
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
                float x = (is(BodySense.BODY_IN_WATER) ? WATER_AIR_DASH_END_BUMP : AIR_DASH_END_BUMP) * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    x *= -1f;
                }
                body.velocity.x += x;
            }
        });

        // ground slide
        c.add(new Behavior() {
            @Override
            protected boolean evaluate(float delta) {
                if (!upgradeHandler.has(MegaAbility.GROUND_SLIDE)) {
                    return false;
                }
                if (is(BehaviorType.GROUND_SLIDING) && is(BodySense.HEAD_TOUCHING_BLOCK)) {
                    return true;
                }
                if (isDamaged() || groundSlideTimer.isFinished() || !is(BodySense.FEET_ON_GROUND) ||
                        !ctrlMan.isPressed(isUpsideDown() ? CtrlBtn.DPAD_UP : CtrlBtn.DPAD_DOWN)) {
                    return false;
                }
                return is(BehaviorType.GROUND_SLIDING) ? ctrlMan.isPressed(CtrlBtn.A) :
                        ctrlMan.isJustPressed(CtrlBtn.A);
            }

            @Override
            protected void init() {
                c.set(BehaviorType.GROUND_SLIDING, true);

                // in body pre-process, body height is reduced from .95f to .45f when ground sliding;
                // when upside down, need to compensate, otherwise Megaman will be off ground
                if (isUpsideDown()) {
                    body.bounds.y += .5f * WorldVals.PPM;
                }
            }

            @Override
            protected void act(float delta) {
                groundSlideTimer.update(delta);
                if (isDamaged() || (is(Facing.LEFT) && is(BodySense.SIDE_TOUCHING_BLOCK_LEFT)) ||
                        (is(Facing.RIGHT) && is(BodySense.SIDE_TOUCHING_BLOCK_RIGHT))) {
                    return;
                }
                float x = (is(BodySense.BODY_IN_WATER) ? WATER_GROUND_SLIDE_VEL : GROUND_SLIDE_VEL) * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    x *= -1f;
                }
                body.velocity.x = x;
            }

            @Override
            protected void end() {
                groundSlideTimer.reset();
                c.set(BehaviorType.GROUND_SLIDING, false);
                float endDash = (is(BodySense.BODY_IN_WATER) ? 2f : 5f) * WorldVals.PPM;
                if (is(Facing.LEFT)) {
                    endDash *= -1;
                }
                body.velocity.x += endDash;
            }
        });

        // climb
        c.add(new Behavior() {

            private Ladder ladder;

            @Override
            protected boolean evaluate(float delta) {
                if (isDamaged() || isAny(BehaviorType.JUMPING, BehaviorType.AIR_DASHING, BehaviorType.GROUND_SLIDING) ||
                        (ladder = body.getUserData(SpecialFactory.LADDER, Ladder.class)) == null) {
                    return false;
                }
                float centerY = body.getCenter().y;
                if (is(BehaviorType.CLIMBING)) {
                    if (!is(BodySense.HEAD_TOUCHING_LADDER)) {
                        if (isUpsideDown() && centerY + .15f * WorldVals.PPM < ladder.body.getY()) {
                            return false;
                        } else if (centerY - .15f * WorldVals.PPM > ladder.body.getMaxY()) {
                            return false;
                        }
                    }
                    if (!is(BodySense.FEET_TOUCHING_LADDER)) {
                        if (isUpsideDown() && centerY - .15f * WorldVals.PPM > ladder.body.getMaxY()) {
                            return false;
                        } else if (centerY + .15f * WorldVals.PPM < ladder.body.getY()) {
                            return false;
                        }
                    }
                    if (ctrlMan.isJustPressed(CtrlBtn.A)) {
                        return false;
                    }
                    return true;
                }
                // TODO: test
                if (is(BodySense.FEET_TOUCHING_LADDER) &&
                        ctrlMan.isPressed(isUpsideDown() ? CtrlBtn.DPAD_UP : CtrlBtn.DPAD_DOWN)) {
                    return true;
                }
                if (is(BodySense.HEAD_TOUCHING_LADDER) &&
                        ctrlMan.isPressed(isUpsideDown() ? CtrlBtn.DPAD_DOWN : CtrlBtn.DPAD_UP)) {
                    return true;
                }
                /*
                if (is(BodySense.FEET_TOUCHING_LADDER) && ctrlMan.isPressed(CtrlBtn.DPAD_DOWN)) {
                    return true;
                } else if (is(BodySense.HEAD_TOUCHING_LADDER) && ctrlMan.isPressed(CtrlBtn.DPAD_UP)) {
                    return true;
                }
                 */
                return false;
            }

            @Override
            protected void init() {
                aButtonTask = null;
                body.gravityOn = false;
                body.collisionOn = false;
                c.set(BehaviorType.CLIMBING, true);
                body.setCenterX(ladder.body.getCenter().x);
                if (body.getMaxY() <= ladder.body.getY()) {
                    body.setY(ladder.body.getY());
                } else if (body.getY() >= ladder.body.getMaxY()) {
                    body.setMaxY(ladder.body.getMaxY());
                }
                body.velocity.setZero();
            }

            @Override
            protected void act(float delta) {
                body.setCenterX(ladder.body.getCenter().x);
                if (isShooting()) {
                    body.velocity.setZero();
                    return;
                }
                if (ctrlMan.isPressed(CtrlBtn.DPAD_UP)) {
                    body.velocity.y = CLIMB_VEL * WorldVals.PPM;
                } else if (ctrlMan.isPressed(CtrlBtn.DPAD_DOWN)) {
                    body.velocity.y = -CLIMB_VEL * WorldVals.PPM;
                } else {
                    body.velocity.y = 0f;
                }
            }

            @Override
            protected void end() {
                body.gravityOn = true;
                body.collisionOn = true;
                body.velocity.setZero();
                c.set(BehaviorType.CLIMBING, false);
                aButtonTask = is(BodySense.BODY_IN_WATER) ? AButtonTask.SWIM : AButtonTask.AIR_DASH;
            }

        });
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

    private SpriteComponent spriteComponent() {
        sprite.setSize(2.475f * WorldVals.PPM, 1.875f * WorldVals.PPM);
        SpriteHandle handle = new SpriteHandle(sprite);
        handle.priority = 4;
        handle.updatable = delta -> {
            handle.hidden = !ready;
            handle.setPosition(body.bounds, isUpsideDown() ? Position.TOP_CENTER : Position.BOTTOM_CENTER);
            sprite.setAlpha(isInvincible() ? (recoveryBlink ? 0f : 1f) : 1f);
            if (is(BehaviorType.GROUND_SLIDING)) {
                float translateY = (isUpsideDown() ? .1f : -.1f) * WorldVals.PPM;
                sprite.translateY(translateY);
            }
            sprite.setFlip(false, isUpsideDown());
        };
        return new SpriteComponent(handle);
    }

}
