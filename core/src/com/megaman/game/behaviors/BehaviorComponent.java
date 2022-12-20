package com.megaman.game.behaviors;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;
import com.megaman.game.utils.interfaces.Updatable;

import java.util.EnumSet;
import java.util.Set;

public class BehaviorComponent implements Component {

    public Set<BehaviorType> activeBehaviors = EnumSet.noneOf(BehaviorType.class);
    public Array<Behavior> behaviors = new Array<>();
    public Updatable preProcess;
    public Updatable postProcess;

    public boolean is(BehaviorType type) {
        return activeBehaviors.contains(type);
    }

    public void set(BehaviorType type, boolean is) {
        if (is) {
            activeBehaviors.add(type);
        } else {
            activeBehaviors.remove(type);
        }
    }

}
