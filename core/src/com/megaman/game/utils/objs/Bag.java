package com.megaman.game.utils.objs;

import com.badlogic.gdx.utils.Array;

import java.util.Iterator;
import java.util.function.Consumer;

public class Bag<T> implements Iterable<T> {

    public Array<T> array = new Array<>();

    @SafeVarargs
    public Bag(T... ts) {
        for (T t : ts) {
            array.add(t);
        }
    }

    @SafeVarargs
    public static <T> Bag<T> of(T... ts) {
        return new Bag<>(ts);
    }

    @Override
    public Iterator<T> iterator() {
        return array.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        array.forEach(action);
    }

}
