package com.megaman.game.entities.megaman.weapons;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.GameEngine;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.entities.EntityFactories;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.Facing;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.megaman.MegamanVals;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.entities.projectiles.ProjectileFactory;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.world.BodySense;
import com.megaman.game.world.WorldVals;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@RequiredArgsConstructor
public class MegamanWeaponHandler {

    @AllArgsConstructor
    private static class MegaWeaponEntry {
        private int ammo;
        private boolean chargeable;
    }

    private static final float MEGA_BUSTER_BULLET_VEL = 10f;
    private static final Vector2 FLAME_TOSS_TRAJECTORY = new Vector2(35f, 10f);

    private final Megaman megaman;
    private final GameEngine gameEngine;
    private final EntityFactories factories;
    private final Map<MegamanWeapon, MegaWeaponEntry> weapons = new EnumMap<>(MegamanWeapon.class);

    public boolean hasWeapon(MegamanWeapon weapon) {
        return weapons.containsKey(weapon);
    }

    public boolean canFireWeapon(MegamanWeapon weapon, MegaChargeStatus stat) {
        if (!hasWeapon(weapon)) {
            return false;
        }
        int cost = weapon == MegamanWeapon.MEGA_BUSTER ? 0 : switch (stat) {
            case FULLY_CHARGED -> weapon.fullyChargedCost;
            case HALF_CHARGED -> weapon.halfChargedCost;
            case NOT_CHARGED -> weapon.cost;
        };
        return cost <= getAmmo(weapon);
    }

    public void putWeapon(MegamanWeapon weapon, boolean chargeable) {
        putWeapon(weapon, MegamanVals.MAX_WEAPON_AMMO, chargeable);
    }

    public void putWeapon(MegamanWeapon weapon, int ammo, boolean chargeable) {
        if (ammo > MegamanVals.MAX_WEAPON_AMMO) {
            ammo = MegamanVals.MAX_WEAPON_AMMO;
        } else if (ammo < 0) {
            ammo = 0;
        }
        weapons.put(weapon, new MegaWeaponEntry(ammo, chargeable));
    }

    public void translateAmmo(MegamanWeapon weapon, int delta) {
        if (!hasWeapon(weapon)) {
            throw new IllegalStateException("Megaman does not have the weapon: " + weapon);
        }
        MegaWeaponEntry e = weapons.get(weapon);
        e.ammo += delta;
        if (e.ammo >= MegamanVals.MAX_WEAPON_AMMO) {
            e.ammo = MegamanVals.MAX_WEAPON_AMMO;
        } else if (e.ammo < 0) {
            e.ammo = 0;
        }
    }

    public void setToMaxAmmo(MegamanWeapon weapon) {
        if (!hasWeapon(weapon)) {
            throw new IllegalStateException("Megaman does not have the weapon: " + weapon);
        }
        MegaWeaponEntry e = weapons.get(weapon);
        e.ammo = MegamanVals.MAX_WEAPON_AMMO;
    }

    public void depleteAmmo(MegamanWeapon weapon) {
        if (!hasWeapon(weapon)) {
            throw new IllegalStateException("Megaman does not have the weapon: " + weapon);
        }
        MegaWeaponEntry e = weapons.get(weapon);
        e.ammo = 0;
    }

    public int getAmmo(MegamanWeapon weapon) {
        if (weapon == MegamanWeapon.MEGA_BUSTER) {
            return Integer.MAX_VALUE;
        }
        return weapons.get(weapon).ammo;
    }

    public boolean isChargeable(MegamanWeapon weapon) {
        return hasWeapon(weapon) && weapons.get(weapon).chargeable;
    }

    public boolean fireWeapon(MegamanWeapon weapon, MegaChargeStatus stat) {
        if (!hasWeapon(weapon)) {
            return false;
        }
        if (!isChargeable(weapon)) {
            stat = MegaChargeStatus.NOT_CHARGED;
        }
        int cost = weapon == MegamanWeapon.MEGA_BUSTER ? 0 : switch (stat) {
            case FULLY_CHARGED -> weapon.fullyChargedCost;
            case HALF_CHARGED -> weapon.halfChargedCost;
            case NOT_CHARGED -> weapon.cost;
        };
        if (cost > getAmmo(weapon)) {
            return false;
        }
        switch (weapon) {
            case MEGA_BUSTER -> fireMegaBuster(stat);
            case FLAME_TOSS -> fireFlameToss(stat);
        }
        translateAmmo(weapon, -cost);
        return true;
    }

    private Vector2 getSpawnCenter() {
        Vector2 spawnCenter = new Vector2(megaman.body.getCenter());
        float xOffset = WorldVals.PPM;
        if (megaman.is(Facing.LEFT)) {
            xOffset *= -1f;
        }
        spawnCenter.x += xOffset;
        float yOffset = WorldVals.PPM / 16f;
        if (megaman.is(BehaviorType.WALL_SLIDING)) {
            yOffset += .15f * WorldVals.PPM;
        } else if (megaman.is(BodySense.FEET_ON_GROUND)) {
            yOffset -= .05f * WorldVals.PPM;
        } else {
            yOffset += WorldVals.PPM / 4f;
        }
        spawnCenter.y += yOffset;
        return spawnCenter;
    }

    private void fireMegaBuster(MegaChargeStatus stat) {
        float x = MEGA_BUSTER_BULLET_VEL;
        if (megaman.is(Facing.LEFT)) {
            x *= -1f;
        }
        Vector2 trajectory = new Vector2(x, 0f);
        ObjectMap<String, Object> data = new ObjectMap<>();
        data.put(ConstKeys.OWNER, megaman);
        data.put(ConstKeys.TRAJECTORY, trajectory);
        Projectile projectile = switch (stat) {
            case NOT_CHARGED -> (Bullet) factories.fetch(EntityType.PROJECTILE, ProjectileFactory.BULLET);
            case HALF_CHARGED, FULLY_CHARGED -> {
                data.put(ConstKeys.BOOL, stat == MegaChargeStatus.FULLY_CHARGED);
                yield (ChargedShot) factories.fetch(EntityType.PROJECTILE, ProjectileFactory.CHARGED_SHOT);
            }
        };
        Vector2 s = getSpawnCenter();
        if (stat == MegaChargeStatus.NOT_CHARGED) {
            megaman.request(SoundAsset.MEGA_BUSTER_BULLET_SHOT_SOUND);
        } else {
            megaman.request(SoundAsset.MEGA_BUSTER_CHARGED_SHOT_SOUND);
            megaman.stopLoop(SoundAsset.MEGA_BUSTER_CHARGING_SOUND);
            s.y += WorldVals.PPM / 10f;
        }
        gameEngine.spawnEntity(projectile, s, data);
    }

    private void fireFlameToss(MegaChargeStatus chargeStatus) {
    }

}
