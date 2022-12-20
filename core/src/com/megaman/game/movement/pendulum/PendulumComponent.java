package com.megaman.game.movement.pendulum;

import com.megaman.game.Component;
import com.megaman.game.utils.interfaces.UpdatableConsumer;

public class PendulumComponent implements Component {

    public Pendulum pendulum;
    public UpdatableConsumer<Pendulum> updatableConsumer;

}
