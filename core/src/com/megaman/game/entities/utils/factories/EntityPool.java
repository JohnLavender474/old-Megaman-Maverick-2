package com.megaman.game.entities.utils.factories;

import com.megaman.game.entities.Entity;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

public class EntityPool {

    private final Supplier<Entity> supplier;
    private final Queue<Entity> queue;

    public EntityPool(int startAmount, Supplier<Entity> supplier) {
        this.supplier = supplier;
        queue = new LinkedList<>();
        for (int i = 0; i < startAmount; i++) {
            pool(supplyNew());
        }
    }

    protected Entity supplyNew() {
        Entity e = supplier.get();
        e.runOnDeath.add(() -> pool(e));
        return e;
    }

    public Entity fetch() {
        return queue.isEmpty() ? supplyNew() : queue.poll();
    }

    public void pool(Entity e) {
        queue.add(e);
    }

}
