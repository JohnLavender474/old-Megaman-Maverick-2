package com.megaman.game.movement.pendulum;

import com.megaman.game.Component;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PendulumComponent implements Component {

    public Pendulum pendulum;
    public Updatable updatable;

    @Override
    public void reset() {
        pendulum.reset();
    }

}
