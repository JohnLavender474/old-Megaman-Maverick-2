package com.megaman.game.entities.megaman.animations;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.Animator;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.world.BodySense;
import com.megaman.game.world.WorldVals;

import java.util.function.Supplier;

public class MegamanAnimator extends Animator {

    private static final String MEGA_BUSTER = "MegaBuster";
    private static final String MAVERICK_BUSTER = "MaverickBuster";
    private static final String FLAME_TOSS = "FlameToss";

    public MegamanAnimator(Megaman megaman) {
        super(megaman.sprite, getKeySupplier(megaman), getAnims(megaman));
    }

    private static ObjectMap<String, Animation> getAnims(Megaman megaman) {
        AssetsManager assMan = megaman.game.getAssMan();
        Array<String> keySet = getAnimKeySet();
        ObjectMap<String, Animation> anims = new ObjectMap<>();

        // Mega Buster
        TextureAtlas megaBusterAtlas = assMan.getTextureAtlas(TextureAsset.MEGAMAN);
        for (String key : keySet) {
            anims.put(MEGA_BUSTER + key, getAnimation(key, megaBusterAtlas));
        }

        // Maverick Buster
        TextureAtlas maverickBusterAtlas = assMan.getTextureAtlas(TextureAsset.MEGAMAN_MAVERICK);
        for (String key : keySet) {
            anims.put(MAVERICK_BUSTER + key, getAnimation(key, maverickBusterAtlas));
        }

        // Flame Toss
        TextureAtlas flameTossAtlas = assMan.getTextureAtlas(TextureAsset.MEGAMAN_FIRE);
        for (String key : keySet) {
            if (UtilMethods.equalsAny(key, "GroundSlideShoot", "SwimAttack", "SwimCharging",
                    "SwimHalfCharging", "SwimShoot")) {
                continue;
            }
            anims.put(FLAME_TOSS + key, getAnimation(key, flameTossAtlas));
        }

        return anims;
    }

    private static Array<String> getAnimKeySet() {
        return new Array<>() {{
            add("Climb");
            add("ClimbShoot");
            add("ClimbHalfCharging");
            add("ClimbCharging");
            add("StillClimb");
            add("StillClimbCharging");
            add("StillClimbHalfCharging");
            add("FinishClimb");
            add("FinishClimbCharging");
            add("FinishClimbHalfCharging");
            add("Stand");
            add("StandCharging");
            add("StandHalfCharging");
            add("StandShoot");
            add("Damaged");
            add("Run");
            add("RunCharging");
            add("RunHalfCharging");
            add("RunShoot");
            add("Jump");
            add("JumpCharging");
            add("JumpHalfCharging");
            add("JumpShoot");
            add("Swim");
            add("SwimAttack");
            add("SwimCharging");
            add("SwimHalfCharging");
            add("SwimShoot");
            add("WallSlide");
            add("WallSlideCharging");
            add("WallSlideHalfCharging");
            add("WallSlideShoot");
            add("GroundSlide");
            add("GroundSlideShoot");
            add("GroundSlideCharging");
            add("GroundSlideHalfCharging");
            add("AirDash");
            add("AirDashCharging");
            add("AirDashHalfCharging");
            add("SlipSlide");
            add("SlipSlideCharging");
            add("SlipSlideHalfCharging");
            add("SlipSlideShoot");
        }};
    }

    private static Animation getAnimation(String key, TextureAtlas t) {
        return switch (key) {
            case "Climb" -> new Animation(t.findRegion("Climb"), 2, .125f);
            case "ClimbShoot" -> new Animation(t.findRegion("ClimbShoot"));
            case "ClimbHalfCharging" -> new Animation(
                    t.findRegion("ClimbHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "ClimbCharging" -> new Animation(
                    t.findRegion("ClimbCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "FinishClimb" -> new Animation(t.findRegion("FinishClimb"));
            case "FinishClimbCharging" -> new Animation(t.findRegion("FinishClimbCharging"), 2, .15f);
            case "FinishClimbHalfCharging" -> new Animation(t.findRegion("FinishClimbHalfCharging"), 2, .15f);
            case "StillClimb" -> new Animation(t.findRegion("StillClimb"));
            case "StillClimbCharging" -> new Animation(t.findRegion("StillClimbCharging"), 2, .15f);
            case "StillClimbHalfCharging" -> new Animation(t.findRegion("StillClimbHalfCharging"), 2, .15f);
            case "Stand" -> new Animation(t.findRegion("Stand"), new float[]{1.5f, .15f});
            case "StandCharging" -> new Animation(
                    t.findRegion("StandCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "StandHalfCharging" -> new Animation(
                    t.findRegion("StandHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "StandShoot" -> new Animation(t.findRegion("StandShoot"));
            case "Damaged" -> new Animation(t.findRegion("Damaged"), 5, .05f);
            case "Run" -> new Animation(t.findRegion("Run"), 4, .125f);
            case "RunCharging" -> new Animation(t
                    .findRegion("RunCharging"), 4, Megaman.CHARGING_ANIM_TIME);
            case "RunHalfCharging" -> new Animation(
                    t.findRegion("RunHalfCharging"), 4, Megaman.CHARGING_ANIM_TIME);
            case "RunShoot" -> new Animation(t.findRegion("RunShoot"), 4, .125f);
            case "Jump" -> new Animation(t.findRegion("Jump"));
            case "JumpCharging" -> new Animation(
                    t.findRegion("JumpCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "JumpHalfCharging" -> new Animation(
                    t.findRegion("JumpHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "JumpShoot" -> new Animation(t.findRegion("JumpShoot"));
            case "Swim" -> new Animation(t.findRegion("Swim"));
            case "SwimAttack" -> new Animation(t.findRegion("SwimAttack"));
            case "SwimCharging" -> new Animation(
                    t.findRegion("SwimCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "SwimHalfCharging" -> new Animation(
                    t.findRegion("SwimHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "SwimShoot" -> new Animation(t.findRegion("SwimShoot"));
            case "WallSlide" -> new Animation(t.findRegion("WallSlide"));
            case "WallSlideCharging" -> new Animation(
                    t.findRegion("WallSlideCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "WallSlideHalfCharging" -> new Animation(
                    t.findRegion("WallSlideHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "WallSlideShoot" -> new Animation(t.findRegion("WallSlideShoot"));
            case "GroundSlide" -> new Animation(t.findRegion("GroundSlide"));
            case "GroundSlideShoot" -> new Animation(t.findRegion("GroundSlideShoot"));
            case "GroundSlideCharging" -> new Animation(
                    t.findRegion("GroundSlideCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "GroundSlideHalfCharging" -> new Animation(
                    t.findRegion("GroundSlideHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "AirDash" -> new Animation(t.findRegion("AirDash"));
            case "AirDashCharging" -> new Animation(
                    t.findRegion("AirDashCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "AirDashHalfCharging" -> new Animation(
                    t.findRegion("AirDashHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "SlipSlide" -> new Animation(t.findRegion("SlipSlide"));
            case "SlipSlideCharging" -> new Animation(
                    t.findRegion("SlipSlideCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "SlipSlideHalfCharging" -> new Animation(
                    t.findRegion("SlipSlideHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "SlipSlideShoot" -> new Animation(t.findRegion("SlipSlideShoot"));
            default -> throw new IllegalStateException("No animation for key: " + key);
        };
    }

    private static Supplier<String> getKeySupplier(Megaman megaman) {
        return () -> {
            String key;
            if (megaman.isDamaged()) {
                key = "Damaged";
            } else if (megaman.is(BehaviorType.CLIMBING)) {
                if (!megaman.is(BodySense.HEAD_TOUCHING_LADDER)) {
                    if (megaman.isShooting()) {
                        key = "ClimbShoot";
                    } else if (megaman.isChargingFully()) {
                        key = "FinishClimbCharging";
                    } else if (megaman.isCharging()) {
                        key = "FinishClimbHalfCharging";
                    } else {
                        key = "FinishClimb";
                    }
                } else if (megaman.body.velocity.y != 0f) {
                    if (megaman.isShooting()) {
                        key = "ClimbShoot";
                    } else if (megaman.isChargingFully()) {
                        key = "ClimbCharging";
                    } else if (megaman.isCharging()) {
                        key = "ClimbHalfCharging";
                    } else {
                        key = "Climb";
                    }
                } else {
                    if (megaman.isShooting()) {
                        key = "ClimbShoot";
                    } else if (megaman.isChargingFully()) {
                        key = "StillClimbCharging";
                    } else if (megaman.isCharging()) {
                        key = "StillClimbHalfCharging";
                    } else {
                        key = "StillClimb";
                    }
                }
            } else if (megaman.is(BehaviorType.AIR_DASHING)) {
                if (megaman.isChargingFully()) {
                    key = "AirDashCharging";
                } else if (megaman.isCharging()) {
                    key = "AirDashHalfCharging";
                } else {
                    key = "AirDash";
                }
            } else if (megaman.is(BehaviorType.GROUND_SLIDING)) {
                if (megaman.isShooting()) {
                    key = "GroundSlideShoot";
                } else if (megaman.isChargingFully()) {
                    key = "GroundSlideCharging";
                } else if (megaman.isCharging()) {
                    key = "GroundSlideHalfCharging";
                } else {
                    key = "GroundSlide";
                }
            } else if (megaman.is(BehaviorType.WALL_SLIDING)) {
                if (megaman.isShooting()) {
                    key = "WallSlideShoot";
                } else if (megaman.isChargingFully()) {
                    key = "WallSlideCharging";
                } else if (megaman.isCharging()) {
                    key = "WallSlideHalfCharging";
                } else {
                    key = "WallSlide";
                }
            } else if (megaman.is(BehaviorType.SWIMMING)) {
                if (megaman.isShooting()) {
                    key = "SwimShoot";
                } else if (megaman.isChargingFully()) {
                    key = "SwimCharging";
                } else if (megaman.isCharging()) {
                    key = "SwimHalfCharging";
                } else {
                    key = "Swim";
                }
            } else if (megaman.is(BehaviorType.JUMPING) || !megaman.is(BodySense.FEET_ON_GROUND)) {
                if (megaman.isShooting()) {
                    key = "JumpShoot";
                } else if (megaman.isChargingFully()) {
                    key = "JumpCharging";
                } else if (megaman.isCharging()) {
                    key = "JumpHalfCharging";
                } else {
                    key = "Jump";
                }
            } else if (megaman.is(BodySense.FEET_ON_GROUND) && megaman.is(BehaviorType.RUNNING)) {
                if (megaman.isShooting()) {
                    key = "RunShoot";
                } else if (megaman.isChargingFully()) {
                    key = "RunCharging";
                } else if (megaman.isCharging()) {
                    key = "RunHalfCharging";
                } else {
                    key = "Run";
                }
            } else if (megaman.is(BodySense.FEET_ON_GROUND) &&
                    Math.abs(megaman.body.velocity.x) > WorldVals.PPM / 16f) {
                if (megaman.isShooting()) {
                    key = "SlipSlideShoot";
                } else if (megaman.isChargingFully()) {
                    key = "SlipSlideCharging";
                } else if (megaman.isCharging()) {
                    key = "SlipSlideHalfCharging";
                } else {
                    key = "SlipSlide";
                }
            } else {
                if (megaman.isShooting()) {
                    key = "StandShoot";
                } else if (megaman.isChargingFully()) {
                    key = "StandCharging";
                } else if (megaman.isCharging()) {
                    key = "StandHalfCharging";
                } else {
                    key = "Stand";
                }
            }
            return getKeyHeader(megaman) + key;
        };
    }

    private static String getKeyHeader(Megaman megaman) {
        return switch (megaman.currWeapon) {
            case MEGA_BUSTER -> megaman.isMaverick() ? MAVERICK_BUSTER : MEGA_BUSTER;
            case FLAME_TOSS -> FLAME_TOSS;
        };
    }

}
