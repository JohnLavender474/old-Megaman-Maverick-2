package com.megaman.game.weapons;

import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.utils.objs.Bag;
import com.megaman.game.utils.objs.Timer;
import lombok.Getter;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public class WeaponDef {

    private final Function<Map<String, Object>, Bag<Projectile>> weaponFunction;
    private final Consumer<Map<String, Object>> runOnShoot;
    private final Timer shootCooldownTimer;

    public WeaponDef(Function<Map<String, Object>, Bag<Projectile>> weaponFunction,
                     float shootCooldown, Consumer<Map<String, Object>> runOnShoot) {
        this.runOnShoot = runOnShoot;
        this.weaponFunction = weaponFunction;
        this.shootCooldownTimer = new Timer(shootCooldown);
        this.shootCooldownTimer.setToEnd();
    }

    public void runOnShoot(Map<String, Object> m) {
        runOnShoot.accept(m);
    }

    public Bag<Projectile> getWeaponsInstances(Map<String, Object> m) {
        return weaponFunction.apply(m);
    }

    public void updateCooldownTimer(float delta) {
        shootCooldownTimer.update(delta);
    }

    public void resetCooldownTimer() {
        shootCooldownTimer.reset();
    }

    public boolean isCooldownTimerFinished() {
        return shootCooldownTimer.isFinished();
    }

}
