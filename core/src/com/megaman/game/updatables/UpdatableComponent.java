package com.megaman.game.updatables;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.KeyValuePair;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@NoArgsConstructor
public class UpdatableComponent implements Component {

    public final Array<KeyValuePair<Updatable, Supplier<Boolean>>> updatables = new Array<>();

    public UpdatableComponent(Updatable updatable) {
        add(updatable);
    }

    public void add(Updatable updatable) {
        add(updatable, () -> true);
    }

    public void add(Updatable updatable, Supplier<Boolean> qualifier) {
        updatables.add(KeyValuePair.of(updatable, qualifier));
    }

}
