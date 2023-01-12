package com.megaman.game.entities.damage;

public interface Damager {

    default boolean canDamage(Damageable damageable) {
        return true;
    }

    default void onDamageInflictedTo(Damageable damageable) {}

}
