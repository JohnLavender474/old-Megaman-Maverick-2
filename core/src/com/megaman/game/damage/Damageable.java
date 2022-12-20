package com.megaman.game.damage;

import java.util.Set;

public interface Damageable {

    Set<Class<? extends Damager>> getDamagerMaskSet();

    void takeDamageFrom(Damager damager);

    default boolean isInvincible() {
        return false;
    }

    default boolean canBeDamagedBy(Damager damager) {
        return !isInvincible() && getDamagerMaskSet().contains(damager.getClass());
    }

}
