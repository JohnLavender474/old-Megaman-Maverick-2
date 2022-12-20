package com.megaman.game.utils.objs;

public record KeyValuePair<K, V>(K key, V value) {

    public static <T, U> KeyValuePair<T, U> of(T t, U u) {
        return new KeyValuePair<>(t, u);
    }

}
