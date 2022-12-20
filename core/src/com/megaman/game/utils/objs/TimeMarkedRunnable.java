package com.megaman.game.utils.objs;

public record TimeMarkedRunnable(Float time, Runnable runnable) implements Comparable<TimeMarkedRunnable> {

    @Override
    public int compareTo(TimeMarkedRunnable o) {
        return time.compareTo(o.time());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TimeMarkedRunnable t && time.equals(t.time());
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }

}
