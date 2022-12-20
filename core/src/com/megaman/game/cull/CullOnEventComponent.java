package com.megaman.game.cull;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;
import com.megaman.game.events.Event;
import lombok.NoArgsConstructor;

import java.util.function.Predicate;

@NoArgsConstructor
public class CullOnEventComponent implements Component {

    public final Array<Predicate<Event>> preds = new Array<>();

    @SafeVarargs
    public CullOnEventComponent(Predicate<Event>... preds) {
        for (Predicate<Event> p : preds) {
            this.preds.add(p);
        }
    }

    public boolean isCullEvent(Event event) {
        for (Predicate<Event> pred : preds) {
            if (pred.test(event)) {
                return true;
            }
        }
        return false;
    }

}
