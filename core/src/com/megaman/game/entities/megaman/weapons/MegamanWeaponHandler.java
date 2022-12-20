package com.megaman.game.entities.megaman.weapons;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.GameEngine;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.entities.EntityFactories;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.Facing;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.megaman.MegamanVals;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.world.BodySense;
import com.megaman.game.world.WorldConstVals;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@RequiredArgsConstructor
public class MegamanWeaponHandler {

    private static final float MEGA_BUSTER_BULLET_VEL = 10f;
    private static final Vector2 FLAME_TOSS_TRAJECTORY = new Vector2(35f, 10f);

    private final Megaman megaman;
    private final GameEngine gameEngine;
    private final EntityFactories factories;
    private final Map<MegamanWeapon, Integer> weapons = new EnumMap<>(MegamanWeapon.class);

    public boolean hasWeapon(MegamanWeapon weapon) {
        return weapons.containsKey(weapon);
    }

    public void putWeapon(MegamanWeapon weapon) {
        putWeapon(weapon, MegamanVals.MAX_WEAPON_AMMO);
    }

    public void putWeapon(MegamanWeapon weapon, int ammo) {
        if (ammo > MegamanVals.MAX_WEAPON_AMMO) {
            ammo = MegamanVals.MAX_WEAPON_AMMO;
        } else if (ammo < 0) {
            ammo = 0;
        }
        weapons.put(weapon, ammo);
    }

    public void translateAmmo(MegamanWeapon weapon, int delta) {
        if (!hasWeapon(weapon)) {
            throw new IllegalStateException("Megaman does not have the weapon: " + weapon);
        }
        int newAmmo = weapons.get(weapon) + delta;
        putWeapon(weapon, newAmmo);
    }

    public void setToMaxAmmo(MegamanWeapon weapon) {
        if (!hasWeapon(weapon)) {
            throw new IllegalStateException("Megaman does not have the weapon: " + weapon);
        }
        weapons.replace(weapon, MegamanVals.MAX_WEAPON_AMMO);
    }

    public void depleteAmmo(MegamanWeapon weapon) {
        if (!hasWeapon(weapon)) {
            throw new IllegalStateException("Megaman does not have the weapon: " + weapon);
        }
        weapons.replace(weapon, 0);
    }

    public int getAmmo(MegamanWeapon weapon) {
        if (weapon == MegamanWeapon.MEGA_BUSTER) {
            return Integer.MAX_VALUE;
        }
        return weapons.get(weapon);
    }

    public boolean fireWeapon(MegamanWeapon weapon, MegaChargeStatus chargeStatus) {
        if (!hasWeapon(weapon)) {
            return false;
        }
        int cost = weapon == MegamanWeapon.MEGA_BUSTER ? 0 : switch (chargeStatus) {
            case FULLY_CHARGED -> weapon.fullyChargedCost;
            case HALF_CHARGED -> weapon.halfChargedCost;
            case NOT_CHARGED -> weapon.cost;
        };
        if (cost > getAmmo(weapon)) {
            return false;
        }
        switch (weapon) {
            case MEGA_BUSTER -> fireMegaBuster(chargeStatus);
            case FLAME_TOSS -> fireFlameToss(chargeStatus);
        }
        translateAmmo(weapon, -cost);
        return true;
    }

    private Vector2 getSpawnCenter() {
        Vector2 spawnCenter = new Vector2(megaman.body.getCenter());
        float xOffset = .75f * WorldConstVals.PPM;
        if (megaman.is(Facing.LEFT)) {
            xOffset *= 1f;
        }
        spawnCenter.x += xOffset;
        float yOffset = WorldConstVals.PPM / 16f;
        if (megaman.is(BehaviorType.WALL_SLIDING)) {
            yOffset += .15f * WorldConstVals.PPM;
        } else if (!megaman.is(BodySense.FEET_ON_GROUND)) {
            yOffset += WorldConstVals.PPM / 4f;
        }
        spawnCenter.y += yOffset;
        return spawnCenter;
    }

    private void fireMegaBuster(MegaChargeStatus chargeStatus) {
        float x = MEGA_BUSTER_BULLET_VEL;
        if (megaman.is(Facing.LEFT)) {
            x *= -1f;
        }
        Vector2 trajectory = new Vector2(x, 0f);
        ObjectMap<String, Object> data = new ObjectMap<>();
        Projectile projectile = switch (chargeStatus) {
            case NOT_CHARGED -> {
                data.put(ConstKeys.OWNER, megaman);
                data.put(ConstKeys.TRAJECTORY, trajectory);
                yield (Bullet) factories.fetch(EntityType.PROJECTILE, "Bullet");
            }
            case HALF_CHARGED, FULLY_CHARGED -> {
                data.put(ConstKeys.OWNER, megaman);
                data.put(ConstKeys.TRAJECTORY, trajectory);
                data.put(ConstKeys.BOOL, chargeStatus == MegaChargeStatus.FULLY_CHARGED);
                yield (ChargedShot) factories.fetch(EntityType.PROJECTILE, "ChargedShot");
            }
        };
        gameEngine.spawnEntity(projectile, getSpawnCenter(), data);
    }

    private void fireFlameToss(MegaChargeStatus chargeStatus) {
        Vector2 trajectory = new Vector2(FLAME_TOSS_TRAJECTORY);
        if (megaman.is(Facing.LEFT)) {
            trajectory.x *= -1f;
        }
        ObjectMap<String, Object> data = new ObjectMap<>();
        // TODO: return projectile
        Projectile projectile = switch (chargeStatus) {
            case NOT_CHARGED -> {
                data.put(ConstKeys.OWNER, megaman);
                data.put(ConstKeys.TRAJECTORY, trajectory);
                yield null;
            }
            default -> null;
        };
        // TODO: spawn projectile
        // gameEngine.spawnEntity(projectile, getSpawnCenter(), data);
    }

}
