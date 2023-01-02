package com.megaman.game.entities.enemies.impl;

public interface UpdateFunc<T, R> {

    R apply(T t, float delta);

}
