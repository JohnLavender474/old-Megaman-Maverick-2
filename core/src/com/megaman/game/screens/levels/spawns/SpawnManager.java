package com.megaman.game.screens.levels.spawns;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.RequiredArgsConstructor;

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
        for (Spawn s : spawns) {
            s.update(delta);
        }
    }

    @Override
    public void reset() {
        spawns = null;
    }

}
