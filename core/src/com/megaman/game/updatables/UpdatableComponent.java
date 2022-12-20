package com.megaman.game.updatables;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.KeyValuePair;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UpdatableComponent implements Component {

    public final Array<KeyValuePair<Updatable, UpdateQualifier>> updatables = new Array<>();

    public UpdatableComponent(Updatable updatable) {
        add(updatable);
    }

    public UpdatableComponent(Updatable updatable, UpdateQualifier qualifier) {
        add(updatable, qualifier);
    }

    public void add(Updatable updatable) {
        add(updatable, new UpdateQualifier() {
            @Override
            public boolean doUpdate() {
                return true;
            }

            @Override
            public boolean doRemove() {
                return false;
            }
        });
    }

    public void add(Updatable updatable, UpdateQualifier qualifier) {
        updatables.add(KeyValuePair.of(updatable, qualifier));
    }

}
