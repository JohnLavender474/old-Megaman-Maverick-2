package com.megaman.game.movement.rotatingline;

import com.megaman.game.Component;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.interfaces.UpdatableConsumer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class RotatingLineComponent implements Component {

    public RotatingLine rotatingLine;
    public Updatable updatable;

    public RotatingLineComponent(RotatingLine rotatingLine) {
        this.rotatingLine = rotatingLine;
    }

}
