package com.megaman.game.shapes;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class LineComponent implements Component {

    public final Array<LineHandle> lineHandles = new Array<>();

    public LineComponent(LineHandle... lineHandles) {
        for (LineHandle lineHandle : lineHandles) {
            this.lineHandles.add(lineHandle);
        }
    }

}
