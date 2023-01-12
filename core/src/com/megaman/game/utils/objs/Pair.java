package com.megaman.game.utils.objs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Consumer;

@Getter
@Setter
@AllArgsConstructor
public class Pair<T> {

    private T first;
    private T second;
    private boolean swappable;

    public Pair() {
        first = null;
        second = null;
    }

    public Pair(T first, T second) {
        this(first, second, true);
    }

    public static <U> Pair<U> of(U u1, U u2) {
        return new Pair<>(u1, u2);
    }

    public void set(Pair<T> p) {
        set(p.getFirst(), p.getSecond());
    }

    public void set(T first, T second) {
        setFirst(first);
        setSecond(second);
    }

    public void forEach(Consumer<T> consumer) {
        consumer.accept(first);
        consumer.accept(second);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair<?> pair)) {
            return false;
        }
        if (first.equals(pair.getFirst())) {
            return second.equals(pair.getSecond());
        }
        if (isSwappable() && first.equals(pair.getSecond())) {
            return second.equals(pair.getFirst());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash += 49 * first.hashCode();
        hash += 49 * second.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + ">";
    }

}

