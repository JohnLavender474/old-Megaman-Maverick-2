package com.megaman.game.movement.pendulum;

import com.megaman.game.Component;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class PendulumComponent implements Component {

    public Pendulum pendulum;
    public Updatable updatable;

    public PendulumComponent(Pendulum pendulum) {
        this.pendulum = pendulum;
    }

}
