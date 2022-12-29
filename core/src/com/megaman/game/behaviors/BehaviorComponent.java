package com.megaman.game.behaviors;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public class BehaviorComponent implements Component {

    private final Set<BehaviorType> activeBehaviors;
    private final Array<Behavior> behaviors;

    public BehaviorComponent() {
        activeBehaviors = EnumSet.noneOf(BehaviorType.class);
        behaviors = new Array<>();
    }

    public void add(Behavior behavior) {
        behaviors.add(behavior);
    }

    public boolean is(BehaviorType type) {
        return activeBehaviors.contains(type);
    }

    public boolean isAny(BehaviorType... behaviorTypes) {
        for (BehaviorType behaviorType : behaviorTypes) {
            if (is(behaviorType)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAll(BehaviorType... behaviorTypes) {
        for (BehaviorType behaviorType : behaviorTypes) {
            if (!is(behaviorType)) {
                return false;
            }
        }
        return true;
    }


    public void set(BehaviorType type, boolean is) {
        if (is) {
            activeBehaviors.add(type);
        } else {
            activeBehaviors.remove(type);
        }
    }

    @Override
    public void reset() {
        activeBehaviors.clear();
        for (Behavior b : behaviors) {
            b.reset();
        }
    }

}
