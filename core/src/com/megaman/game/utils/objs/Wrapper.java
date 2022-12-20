package com.megaman.game.utils.objs;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Wrapper<T> {

    public T data;

    public static <T> Wrapper<T> empty() {
        return new Wrapper<>();
    }

    public static <T> Wrapper<T> of(T data) {
        return new Wrapper<>(data);
    }

}
