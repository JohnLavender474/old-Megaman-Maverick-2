package com.megaman.game.utils.interfaces;

public interface UpdateFunc<T, R> {

    R apply(T t, float delta);

}
