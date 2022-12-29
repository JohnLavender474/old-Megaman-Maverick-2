package com.megaman.game.screens.levels.spawns;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;

@RequiredArgsConstructor
public class SpawnManager implements Updatable, Resettable {

    private Array<Spawn> spawns;

    public void set(Array<Spawn> spawns) {
        this.spawns = spawns;
    }

    @Override
    public void update(float delta) {
        if (spawns == null) {
            throw new IllegalStateException("Set method must first be called with non-null values");
        }
        Iterator<Spawn> spawnIter = spawns.iterator();
        while (spawnIter.hasNext()) {
            Spawn s = spawnIter.next();
            s.update(delta);
            if (s.doRemove()) {
                spawnIter.remove();
            }
        }
    }

    @Override
    public void reset() {
        spawns = null;
    }

}
