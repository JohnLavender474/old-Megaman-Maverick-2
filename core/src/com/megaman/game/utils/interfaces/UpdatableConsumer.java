package com.megaman.game.utils.interfaces;

public interface UpdatableConsumer<T> {

    void consumeAndUpdate(T t, float delta);

}
