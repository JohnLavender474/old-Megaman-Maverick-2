package com.megaman.game.movement.rotatingline;

import com.megaman.game.Component;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RotatingLineComponent implements Component {

    public RotatingLine rotatingLine;
    public Updatable updatable;

    @Override
    public void reset() {
        rotatingLine.reset();
    }

}
