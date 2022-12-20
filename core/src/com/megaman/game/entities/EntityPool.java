package com.megaman.game.entities;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

public class EntityPool<E extends Entity> {

    private final Supplier<E> supplier;
    private final Queue<E> queue = new LinkedList<>();

    public EntityPool(int startAmount, Supplier<E> supplier) {
        this.supplier = supplier;
        for (int i = 0; i < startAmount; i++) {
            pool(supplyNew());
        }
    }

    protected E supplyNew() {
        E e = supplier.get();
        e.runOnDeath.add(() -> pool(e));
        return e;
    }

    public E fetch() {
        return queue.isEmpty() ? supplyNew() : queue.poll();
    }

    public void pool(E e) {
        queue.add(e);
    }

}
