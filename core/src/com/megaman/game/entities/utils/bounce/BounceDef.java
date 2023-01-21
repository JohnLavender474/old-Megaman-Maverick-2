package com.megaman.game.entities.utils.bounce;

import com.badlogic.gdx.math.Vector2;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BounceDef {

    public final Vector2 bounce;
    public final BounceAction xAction;
    public final BounceAction yAction;

    public BounceDef(float bounceX, float bounceY, BounceAction xAction, BounceAction yAction) {
        this(new Vector2(bounceX, bounceY), xAction, yAction);
    }

    public static BounceDef setStill() {
        return setStill(true, true);
    }

    public static BounceDef setStill(boolean xSetStill, boolean ySetStill) {
        return new BounceDef(0f, 0f, xSetStill ? BounceAction.SET : BounceAction.ADD,
                ySetStill ? BounceAction.SET : BounceAction.ADD);
    }

    public static BounceDef noBounce() {
        return new BounceDef(0f, 0f, BounceAction.ADD, BounceAction.ADD);
    }

}
