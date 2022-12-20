package com.megaman.game.shapes;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LineComponent implements Component {

    public final Array<LineHandle> lineHandles = new Array<>();

}
