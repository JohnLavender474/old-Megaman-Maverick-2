package com.megaman.game.entities.impl.megaman.animations;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.Animator;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.entities.impl.megaman.Megaman;
import com.megaman.game.entities.utils.faceable.Facing;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.world.BodySense;
import com.megaman.game.world.WorldVals;

import java.util.function.Supplier;

public class MegamanAnimator extends Animator {

    public static final float CHARGE_DUR = .125f;

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

        // TODO: temp ignore mega buster
        // Mega Buster
        /*
        TextureAtlas megaBusterAtlas = assMan.getTextureAtlas(TextureAsset.MEGAMAN);
        for (String key : keySet) {
            anims.put(MEGA_BUSTER + key, getAnimation(key, megaBusterAtlas));
        }
         */

        // Maverick Buster
        TextureAtlas maverickBusterAtlas = assMan.getTextureAtlas(TextureAsset.MEGAMAN_MAVERICK);
        for (String key : keySet) {
            anims.put(MAVERICK_BUSTER + key, getAnimation(key, maverickBusterAtlas));
        }

        // TODO: temp ignore flame toss
        // Flame Toss
        /*
        TextureAtlas flameTossAtlas = assMan.getTextureAtlas(TextureAsset.MEGAMAN_FIRE);
        for (String key : keySet) {
            if (UtilMethods.equalsAny(key, "GroundSlideShoot", "SwimAttack", "SwimCharging",
                    "SwimHalfCharging", "SwimShoot")) {
                continue;
            }
            anims.put(FLAME_TOSS + key, getAnimation(key, flameTossAtlas));
        }
         */

        return anims;
    }

    private static Array<String> getAnimKeySet() {
        return new Array<>() {{
            add("Climb");
            add("Climb_Left");
            add("ClimbHalfCharging");
            add("ClimbHalfCharging_Left");
            add("ClimbCharging");
            add("ClimbCharging_Left");
            add("ClimbShoot");
            add("ClimbShoot_Left");

            add("StillClimb");
            add("StillClimb_Left");
            add("StillClimbCharging");
            add("StillClimbCharging_Left");
            add("StillClimbHalfCharging");
            add("StillClimbHalfCharging_Left");

            add("FinishClimb");
            add("FinishClimb_Left");
            add("FinishClimbCharging");
            add("FinishClimbCharging_Left");
            add("FinishClimbHalfCharging");
            add("FinishClimbHalfCharging_Left");

            add("Stand");
            add("Stand_Left");
            add("StandCharging");
            add("StandCharging_Left");
            add("StandHalfCharging");
            add("StandHalfCharging_Left");
            add("StandShoot");
            add("StandShoot_Left");

            add("Damaged");
            add("Damaged_Left");

            add("Run");
            add("Run_Left");
            add("RunCharging");
            add("RunCharging_Left");
            add("RunHalfCharging");
            add("RunHalfCharging_Left");
            add("RunShoot");
            add("RunShoot_Left");

            add("Jump");
            add("Jump_Left");
            add("JumpCharging");
            add("JumpCharging_Left");
            add("JumpHalfCharging");
            add("JumpHalfCharging_Left");
            add("JumpShoot");
            add("JumpShoot_Left");

            add("Swim");
            add("Swim_Left");
            add("SwimAttack");
            add("SwimAttack_Left");
            add("SwimCharging");
            add("SwimCharging_Left");
            add("SwimHalfCharging");
            add("SwimHalfCharging_Left");
            add("SwimShoot");
            add("SwimShoot_Left");

            add("WallSlide");
            add("WallSlide_Left");
            add("WallSlideCharging");
            add("WallSlideCharging_Left");
            add("WallSlideHalfCharging");
            add("WallSlideHalfCharging_Left");
            add("WallSlideShoot");
            add("WallSlideShoot_Left");

            add("GroundSlide");
            add("GroundSlide_Left");
            add("GroundSlideShoot");
            add("GroundSlideShoot_Left");
            add("GroundSlideCharging");
            add("GroundSlideCharging_Left");
            add("GroundSlideHalfCharging");
            add("GroundSlideHalfCharging_Left");

            add("AirDash");
            add("AirDash_Left");
            add("AirDashCharging");
            add("AirDashCharging_Left");
            add("AirDashHalfCharging");
            add("AirDashHalfCharging_Left");

            add("SlipSlide");
            add("SlipSlide_Left");
            add("SlipSlideCharging");
            add("SlipSlideCharging_Left");
            add("SlipSlideHalfCharging");
            add("SlipSlideHalfCharging_Left");
            add("SlipSlideShoot");
            add("SlipSlideShoot_Left");
        }};
    }

    private static Animation getAnimation(String key, TextureAtlas t) {
        return switch (key) {
            case "Climb" -> new Animation(t.findRegion("Climb"), 2, .125f);
            case "Climb_Left" -> new Animation(t.findRegion("Climb_Left"), 2, .125f);
            case "ClimbShoot" -> new Animation(t.findRegion("ClimbShoot"));
            case "ClimbShoot_Left" -> new Animation(t.findRegion("ClimbShoot_Left"));
            case "ClimbHalfCharging" -> new Animation(t.findRegion("ClimbHalfCharging"), 2, CHARGE_DUR);
            case "ClimbHalfCharging_Left" -> new Animation(t.findRegion("ClimbHalfCharging_Left"), 2, CHARGE_DUR);
            case "ClimbCharging" -> new Animation(t.findRegion("ClimbCharging"), 2, CHARGE_DUR);
            case "ClimbCharging_Left" -> new Animation(t.findRegion("ClimbCharging_Left"), 2, CHARGE_DUR);

            case "FinishClimb" -> new Animation(t.findRegion("FinishClimb"));
            case "FinishClimb_Left" -> new Animation(t.findRegion("FinishClimb_Left"));
            case "FinishClimbCharging" -> new Animation(t.findRegion("FinishClimbCharging"), 2, .15f);
            case "FinishClimbCharging_Left" -> new Animation(t.findRegion("FinishClimbCharging_Left"), 2, .15f);
            case "FinishClimbHalfCharging" -> new Animation(t.findRegion("FinishClimbHalfCharging"), 2, .15f);
            case "FinishClimbHalfCharging_Left" -> new Animation(t.findRegion("FinishClimbHalfCharging_Left"), 2, .15f);

            case "StillClimb" -> new Animation(t.findRegion("StillClimb"));
            case "StillClimb_Left" -> new Animation(t.findRegion("StillClimb_Left"));
            case "StillClimbCharging" -> new Animation(t.findRegion("StillClimbCharging"), 2, .15f);
            case "StillClimbCharging_Left" -> new Animation(t.findRegion("StillClimbCharging_Left"), 2, .15f);
            case "StillClimbHalfCharging" -> new Animation(t.findRegion("StillClimbHalfCharging"), 2, .15f);
            case "StillClimbHalfCharging_Left" -> new Animation(t.findRegion("StillClimbHalfCharging_Left"), 2, .15f);

            case "Stand" -> new Animation(t.findRegion("Stand"), new float[]{1.5f, .15f});
            case "Stand_Left" -> new Animation(t.findRegion("Stand_Left"), new float[]{1.5f, .15f});
            case "StandCharging" -> new Animation(t.findRegion("StandCharging"), 2, CHARGE_DUR);
            case "StandCharging_Left" -> new Animation(t.findRegion("StandCharging_Left"), 2, CHARGE_DUR);
            case "StandHalfCharging" -> new Animation(t.findRegion("StandHalfCharging"), 2, CHARGE_DUR);
            case "StandHalfCharging_Left" -> new Animation(t.findRegion("StandHalfCharging_Left"), 2, CHARGE_DUR);
            case "StandShoot" -> new Animation(t.findRegion("StandShoot"));
            case "StandShoot_Left" -> new Animation(t.findRegion("StandShoot_Left"));

            case "Damaged" -> new Animation(t.findRegion("Damaged"), 5, .05f);
            case "Damaged_Left" -> new Animation(t.findRegion("Damaged_Left"), 5, .05f);

            case "Run" -> new Animation(t.findRegion("Run"), 4, .125f);
            case "Run_Left" -> new Animation(t.findRegion("Run_Left"), 4, .125f);
            case "RunCharging" -> new Animation(t.findRegion("RunCharging"), 4, CHARGE_DUR);
            case "RunCharging_Left" -> new Animation(t.findRegion("RunCharging_Left"), 4, CHARGE_DUR);
            case "RunHalfCharging" -> new Animation(t.findRegion("RunHalfCharging"), 4, CHARGE_DUR);
            case "RunHalfCharging_Left" -> new Animation(t.findRegion("RunHalfCharging_Left"), 4, CHARGE_DUR);
            case "RunShoot" -> new Animation(t.findRegion("RunShoot"), 4, .125f);
            case "RunShoot_Left" -> new Animation(t.findRegion("RunShoot_Left"), 4, .125f);

            case "Jump" -> new Animation(t.findRegion("Jump"));
            case "Jump_Left" -> new Animation(t.findRegion("Jump_Left"));
            case "JumpCharging" -> new Animation(t.findRegion("JumpCharging"), 2, CHARGE_DUR);
            case "JumpCharging_Left" -> new Animation(t.findRegion("JumpCharging_Left"), 2, CHARGE_DUR);
            case "JumpHalfCharging" -> new Animation(t.findRegion("JumpHalfCharging"), 2, CHARGE_DUR);
            case "JumpHalfCharging_Left" -> new Animation(t.findRegion("JumpHalfCharging_Left"), 2, CHARGE_DUR);
            case "JumpShoot" -> new Animation(t.findRegion("JumpShoot"));
            case "JumpShoot_Left" -> new Animation(t.findRegion("JumpShoot_Left"));

            case "Swim" -> new Animation(t.findRegion("Swim"));
            case "Swim_Left" -> new Animation(t.findRegion("Swim_Left"));
            case "SwimAttack" -> new Animation(t.findRegion("SwimAttack"));
            case "SwimAttack_Left" -> new Animation(t.findRegion("SwimAttack_Left"));
            case "SwimCharging" -> new Animation(t.findRegion("SwimCharging"), 2, CHARGE_DUR);
            case "SwimCharging_Left" -> new Animation(t.findRegion("SwimCharging_Left"), 2, CHARGE_DUR);
            case "SwimHalfCharging" -> new Animation(t.findRegion("SwimHalfCharging"), 2, CHARGE_DUR);
            case "SwimHalfCharging_Left" -> new Animation(t.findRegion("SwimHalfCharging_Left"), 2, CHARGE_DUR);
            case "SwimShoot" -> new Animation(t.findRegion("SwimShoot"));
            case "SwimShoot_Left" -> new Animation(t.findRegion("SwimShoot_Left"));

            case "WallSlide" -> new Animation(t.findRegion("WallSlide"));
            case "WallSlide_Left" -> new Animation(t.findRegion("WallSlide_Left"));
            case "WallSlideCharging" -> new Animation(t.findRegion("WallSlideCharging"), 2, CHARGE_DUR);
            case "WallSlideCharging_Left" -> new Animation(t.findRegion("WallSlideCharging_Left"), 2, CHARGE_DUR);
            case "WallSlideHalfCharging" -> new Animation(t.findRegion("WallSlideHalfCharging"), 2, CHARGE_DUR);
            case "WallSlideHalfCharging_Left" -> new Animation(
                    t.findRegion("WallSlideHalfCharging_Left"), 2, CHARGE_DUR);
            case "WallSlideShoot" -> new Animation(t.findRegion("WallSlideShoot"));
            case "WallSlideShoot_Left" -> new Animation(t.findRegion("WallSlideShoot_Left"));

            case "GroundSlide" -> new Animation(t.findRegion("GroundSlide"));
            case "GroundSlide_Left" -> new Animation(t.findRegion("GroundSlide_Left"));
            case "GroundSlideShoot" -> new Animation(t.findRegion("GroundSlideShoot"));
            case "GroundSlideShoot_Left" -> new Animation(t.findRegion("GroundSlideShoot_Left"));
            case "GroundSlideCharging" -> new Animation(t.findRegion("GroundSlideCharging"), 2, CHARGE_DUR);
            case "GroundSlideCharging_Left" -> new Animation(t.findRegion("GroundSlideCharging_Left"), 2, CHARGE_DUR);
            case "GroundSlideHalfCharging" -> new Animation(t.findRegion("GroundSlideHalfCharging"), 2, CHARGE_DUR);
            case "GroundSlideHalfCharging_Left" -> new Animation(
                    t.findRegion("GroundSlideHalfCharging_Left"), 2, CHARGE_DUR);

            case "AirDash" -> new Animation(t.findRegion("AirDash"));
            case "AirDash_Left" -> new Animation(t.findRegion("AirDash_Left"));
            case "AirDashCharging" -> new Animation(t.findRegion("AirDashCharging"), 2, CHARGE_DUR);
            case "AirDashCharging_Left" -> new Animation(t.findRegion("AirDashCharging_Left"), 2, CHARGE_DUR);
            case "AirDashHalfCharging" -> new Animation(t.findRegion("AirDashHalfCharging"), 2, CHARGE_DUR);
            case "AirDashHalfCharging_Left" -> new Animation(t.findRegion("AirDashHalfCharging_Left"), 2, CHARGE_DUR);

            case "SlipSlide" -> new Animation(t.findRegion("SlipSlide"));
            case "SlipSlide_Left" -> new Animation(t.findRegion("SlipSlide_Left"));
            case "SlipSlideCharging" -> new Animation(t.findRegion("SlipSlideCharging"), 2, CHARGE_DUR);
            case "SlipSlideCharging_Left" -> new Animation(t.findRegion("SlipSlideCharging_Left"), 2, CHARGE_DUR);
            case "SlipSlideHalfCharging" -> new Animation(t.findRegion("SlipSlideHalfCharging"), 2, CHARGE_DUR);
            case "SlipSlideHalfCharging_Left" -> new Animation(
                    t.findRegion("SlipSlideHalfCharging_Left"), 2, CHARGE_DUR);
            case "SlipSlideShoot" -> new Animation(t.findRegion("SlipSlideShoot"));
            case "SlipSlideShoot_Left" -> new Animation(t.findRegion("SlipSlideShoot_Left"));

            default -> throw new IllegalStateException("No animation for key: " + key);
        };
    }

    private static Supplier<String> getKeySupplier(Megaman megaman) {
        return () -> {
            String t;
            if (megaman.isDamaged()) {
                t = "Damaged";
            } else if (megaman.is(BehaviorType.CLIMBING)) {
                if (!megaman.is(BodySense.HEAD_TOUCHING_LADDER)) {
                    if (megaman.isShooting()) {
                        t = "ClimbShoot";
                    } else if (megaman.isChargingFully()) {
                        t = "FinishClimbCharging";
                    } else if (megaman.isCharging()) {
                        t = "FinishClimbHalfCharging";
                    } else {
                        t = "FinishClimb";
                    }
                } else if (megaman.body.velocity.y != 0f) {
                    if (megaman.isShooting()) {
                        t = "ClimbShoot";
                    } else if (megaman.isChargingFully()) {
                        t = "ClimbCharging";
                    } else if (megaman.isCharging()) {
                        t = "ClimbHalfCharging";
                    } else {
                        t = "Climb";
                    }
                } else {
                    if (megaman.isShooting()) {
                        t = "ClimbShoot";
                    } else if (megaman.isChargingFully()) {
                        t = "StillClimbCharging";
                    } else if (megaman.isCharging()) {
                        t = "StillClimbHalfCharging";
                    } else {
                        t = "StillClimb";
                    }
                }
            } else if (megaman.is(BehaviorType.AIR_DASHING)) {
                if (megaman.isChargingFully()) {
                    t = "AirDashCharging";
                } else if (megaman.isCharging()) {
                    t = "AirDashHalfCharging";
                } else {
                    t = "AirDash";
                }
            } else if (megaman.is(BehaviorType.GROUND_SLIDING)) {
                if (megaman.isShooting()) {
                    t = "GroundSlideShoot";
                } else if (megaman.isChargingFully()) {
                    t = "GroundSlideCharging";
                } else if (megaman.isCharging()) {
                    t = "GroundSlideHalfCharging";
                } else {
                    t = "GroundSlide";
                }
            } else if (megaman.is(BehaviorType.WALL_SLIDING)) {
                if (megaman.isShooting()) {
                    t = "WallSlideShoot";
                } else if (megaman.isChargingFully()) {
                    t = "WallSlideCharging";
                } else if (megaman.isCharging()) {
                    t = "WallSlideHalfCharging";
                } else {
                    t = "WallSlide";
                }
            } else if (megaman.is(BehaviorType.SWIMMING)) {
                if (megaman.isShooting()) {
                    t = "SwimShoot";
                } else if (megaman.isChargingFully()) {
                    t = "SwimCharging";
                } else if (megaman.isCharging()) {
                    t = "SwimHalfCharging";
                } else {
                    t = "Swim";
                }
            } else if (megaman.is(BehaviorType.JUMPING) || !megaman.is(BodySense.FEET_ON_GROUND)) {
                if (megaman.isShooting()) {
                    t = "JumpShoot";
                } else if (megaman.isChargingFully()) {
                    t = "JumpCharging";
                } else if (megaman.isCharging()) {
                    t = "JumpHalfCharging";
                } else {
                    t = "Jump";
                }
            } else if (megaman.is(BodySense.FEET_ON_GROUND) && megaman.is(BehaviorType.RUNNING)) {
                if (megaman.isShooting()) {
                    t = "RunShoot";
                } else if (megaman.isChargingFully()) {
                    t = "RunCharging";
                } else if (megaman.isCharging()) {
                    t = "RunHalfCharging";
                } else {
                    t = "Run";
                }
            } else if (megaman.is(BodySense.FEET_ON_GROUND) &&
                    Math.abs(megaman.body.velocity.x) > WorldVals.PPM / 16f) {
                if (megaman.isShooting()) {
                    t = "SlipSlideShoot";
                } else if (megaman.isChargingFully()) {
                    t = "SlipSlideCharging";
                } else if (megaman.isCharging()) {
                    t = "SlipSlideHalfCharging";
                } else {
                    t = "SlipSlide";
                }
            } else {
                if (megaman.isShooting()) {
                    t = "StandShoot";
                } else if (megaman.isChargingFully()) {
                    t = "StandCharging";
                } else if (megaman.isCharging()) {
                    t = "StandHalfCharging";
                } else {
                    t = "Stand";
                }
            }
            String key = getKeyHeader(megaman) + t;
            return megaman.is(Facing.LEFT) ? key + "_Left" : key;
        };
    }

    private static String getKeyHeader(Megaman megaman) {
        return MAVERICK_BUSTER;
        // TODO: temp ignore all except mav buster
        /*
        return switch (megaman.currWeapon) {
            case MEGA_BUSTER -> megaman.isMaverick() ? MAVERICK_BUSTER : MEGA_BUSTER;
            case FLAME_TOSS -> FLAME_TOSS;
        };
         */
    }

}
