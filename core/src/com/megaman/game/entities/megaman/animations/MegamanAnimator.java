package com.megaman.game.entities.megaman.animations;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedSet;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.Animator;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.megaman.weapons.MegamanWeapon;
import com.megaman.game.world.BodySense;
import com.megaman.game.world.WorldVals;

import java.util.function.Supplier;

public class MegamanAnimator {

    public static Animator getAnimator(Megaman megaman) {
        ObjectMap<String, Animation> anims = new ObjectMap<>();
        for (MegamanWeapon weapon : MegamanWeapon.values()) {
            putAnims(megaman, weapon, weapon.megamanAss, anims);
        }
        return new Animator(megaman.sprite, getKeySupplier(megaman), anims);
    }

    private static void putAnims(Megaman megaman, MegamanWeapon weapon, TextureAsset asset,
                                 ObjectMap<String, Animation> anims) {
        OrderedSet<String> animKeySet = getAnimKeySet();
        OrderedSet<String> animKeysToRemove = getAnimsKeysToExclude(weapon);
        if (animKeysToRemove != null) {
            for (String key : animKeysToRemove) {
                animKeySet.remove(key);
            }
        }
        TextureAtlas atlas = megaman.game.getAssMan().getTextureAtlas(asset);
        for (String key : animKeySet) {
            Animation anim = getAnimation(key, atlas);
            anims.put(weapon.name() + key, anim);
        }
    }

    private static OrderedSet<String> getAnimKeySet() {
        return new OrderedSet<>() {{
            add("Climb");
            add("ClimbShoot");
            add("ClimbHalfCharging");
            add("ClimbCharging");
            add("Stand");
            add("StandCharging");
            add("StandHalfCharging");
            add("StandShoot");
            add("Damaged");
            add("LayDownDamaged");
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

    private static OrderedSet<String> getAnimsKeysToExclude(MegamanWeapon weapon) {
        return switch (weapon) {
            case FLAME_TOSS -> new OrderedSet<>() {{
                add("SwimAttack");
                add("SwimCharging");
                add("SwimHalfCharging");
                add("SwimShoot");
            }};
            default -> null;
        };
    }

    private static Animation getAnimation(String key, TextureAtlas textureAtlas) {
        return switch (key) {
            case "Climb" -> new Animation(textureAtlas.findRegion("Climb"), 2, .125f);
            case "ClimbShoot" -> new Animation(textureAtlas.findRegion("ClimbShoot"));
            case "ClimbHalfCharging" -> new Animation(
                    textureAtlas.findRegion("ClimbHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "ClimbCharging" -> new Animation(
                    textureAtlas.findRegion("ClimbCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "Stand" -> new Animation(textureAtlas.findRegion("Stand"), new float[]{1.5f, .15f});
            case "StandCharging" -> new Animation(
                    textureAtlas.findRegion("StandCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "StandHalfCharging" -> new Animation(
                    textureAtlas.findRegion("StandHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "StandShoot" -> new Animation(textureAtlas.findRegion("StandShoot"));
            // damaged
            case "Damaged" -> new Animation(textureAtlas.findRegion("Damaged"), 3, .05f);
            case "LayDownDamaged" -> new Animation(textureAtlas.findRegion("LayDownDamaged"), 3, .05f);
            // run
            case "Run" -> new Animation(textureAtlas.findRegion("Run"), 4, .125f);
            case "RunCharging" -> new Animation(textureAtlas
                    .findRegion("RunCharging"), 4, Megaman.CHARGING_ANIM_TIME);
            case "RunHalfCharging" -> new Animation(
                    textureAtlas.findRegion("RunHalfCharging"), 4, Megaman.CHARGING_ANIM_TIME);
            case "RunShoot" -> new Animation(textureAtlas.findRegion("RunShoot"), 4, .125f);
            // jump
            case "Jump" -> new Animation(textureAtlas.findRegion("Jump"));
            case "JumpCharging" -> new Animation(
                    textureAtlas.findRegion("JumpCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "JumpHalfCharging" -> new Animation(
                    textureAtlas.findRegion("JumpHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "JumpShoot" -> new Animation(textureAtlas.findRegion("JumpShoot"));
            // swim
            case "Swim" -> new Animation(textureAtlas.findRegion("Swim"));
            case "SwimAttack" -> new Animation(textureAtlas.findRegion("SwimAttack"));
            case "SwimCharging" -> new Animation(
                    textureAtlas.findRegion("SwimCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "SwimHalfCharging" -> new Animation(
                    textureAtlas.findRegion("SwimHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "SwimShoot" -> new Animation(textureAtlas.findRegion("SwimShoot"));
            // wall slide
            case "WallSlide" -> new Animation(textureAtlas.findRegion("WallSlide"));
            case "WallSlideCharging" -> new Animation(
                    textureAtlas.findRegion("WallSlideCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "WallSlideHalfCharging" -> new Animation(
                    textureAtlas.findRegion("WallSlideHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "WallSlideShoot" -> new Animation(textureAtlas.findRegion("WallSlideShoot"));
            // ground slide
            case "GroundSlide" -> new Animation(textureAtlas.findRegion("GroundSlide"));
            case "GroundSlideCharging" -> new Animation(
                    textureAtlas.findRegion("GroundSlideCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "GroundSlideHalfCharging" -> new Animation(
                    textureAtlas.findRegion("GroundSlideHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            // air dash
            case "AirDash" -> new Animation(textureAtlas.findRegion("AirDash"));
            case "AirDashCharging" -> new Animation(
                    textureAtlas.findRegion("AirDashCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "AirDashHalfCharging" -> new Animation(
                    textureAtlas.findRegion("AirDashHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            // slip slide
            case "SlipSlide" -> new Animation(textureAtlas.findRegion("SlipSlide"));
            case "SlipSlideCharging" -> new Animation(
                    textureAtlas.findRegion("SlipSlideCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "SlipSlideHalfCharging" -> new Animation(
                    textureAtlas.findRegion("SlipSlideHalfCharging"), 2, Megaman.CHARGING_ANIM_TIME);
            case "SlipSlideShoot" -> new Animation(textureAtlas.findRegion("SlipSlideShoot"));
            default -> throw new IllegalStateException("No animation for key: " + key);
        };
    }

    private static Supplier<String> getKeySupplier(Megaman megaman) {
        return () -> {
            String key;
            if (megaman.isDamaged()) {
                key = megaman.is(BehaviorType.GROUND_SLIDING) ? "LayDownDamaged" : "Damaged";
            } else if (megaman.is(BehaviorType.AIR_DASHING)) {
                if (megaman.isChargingFully()) {
                    key = "AirDashCharging";
                } else if (megaman.isCharging()) {
                    key = "AirDashHalfCharging";
                } else {
                    key = "AirDash";
                }
            } else if (megaman.is(BehaviorType.GROUND_SLIDING)) {
                if (megaman.isChargingFully()) {
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
            } else if (megaman.is(BehaviorType.CLIMBING)) {
                if (megaman.isShooting()) {
                    key = "ClimbShoot";
                } else if (megaman.isChargingFully()) {
                    key = "ClimbCharging";
                } else if (megaman.isCharging()) {
                    key = "ClimbHalfCharging";
                } else {
                    key = "Climb";
                }
            } else if (megaman.is(BodySense.FEET_ON_GROUND) &&
                    Math.abs(megaman.body.velocity.x) > WorldVals.PPM / 8f) {
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
            return megaman.currWeapon.name() + key;
        };
    }

}
