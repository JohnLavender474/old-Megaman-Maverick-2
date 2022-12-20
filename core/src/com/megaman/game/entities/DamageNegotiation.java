package com.megaman.game.entities;

import java.util.function.Function;

public class DamageNegotiation {

    public Function<Damager, Integer> damageFunction;
    public Runnable runOnDamage;

    public DamageNegotiation(int damage) {
        this(damage, () -> {});
    }

    public DamageNegotiation(int damage, Runnable runOnDamage) {
        this(damager -> damage, runOnDamage);
    }

    public DamageNegotiation(Function<Damager, Integer> damageFunction) {
        this(damageFunction, () -> {});
    }

    public DamageNegotiation(Function<Damager, Integer> damageFunction, Runnable runOnDamage) {
        this.damageFunction = damageFunction;
        this.runOnDamage = runOnDamage;
    }

    public int getDamage(Damager damager) {
        return damageFunction.apply(damager);
    }

    public void runOnDamage() {
        runOnDamage.run();
    }

}
