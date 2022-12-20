package com.megaman.game.animations;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class Animator implements Updatable {

    private final Sprite sprite;
    private final Supplier<String> animKeySupplier;
    private final Function<String, Animation> animFunc;

    public Animator(Sprite sprite, Animation anim) {
        this(sprite, () -> "", key -> anim);
    }

    private String currAnimKey;

    @Override
    public void update(float delta) {
        String priorAnimKey = currAnimKey;
        String newAnimKey = animKeySupplier.get();
        currAnimKey = newAnimKey != null ? newAnimKey : priorAnimKey;
        Animation timedAnimation = animFunc.apply(currAnimKey);
        if (timedAnimation == null) {
            return;
        }
        timedAnimation.update(delta);
        sprite.setRegion(timedAnimation.getCurrentRegion());
        if (priorAnimKey != null && !currAnimKey.equals(priorAnimKey)) {
            Animation priorAnimation = animFunc.apply(priorAnimKey);
            priorAnimation.reset();
        }
    }

}
