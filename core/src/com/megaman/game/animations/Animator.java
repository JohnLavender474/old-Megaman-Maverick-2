package com.megaman.game.animations;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class Animator implements Updatable, Resettable {

    public static final String DEFAULT = "default";

    private final Sprite sprite;
    private final Supplier<String> animKeySupplier;
    private final ObjectMap<String, Animation> anims;

    private String currAnimKey;

    public Animator(Sprite sprite, Animation anim) {
        this(sprite, () -> DEFAULT, new ObjectMap<>() {{
            put(DEFAULT, anim);
        }});
    }

    @Override
    public void update(float delta) {
        String priorAnimKey = currAnimKey;
        String newAnimKey = animKeySupplier.get();
        if (newAnimKey != null) {
            currAnimKey = newAnimKey;
        }
        Animation anim = anims.get(currAnimKey);
        if (anim == null) {
            return;
        }
        anim.update(delta);
        sprite.setRegion(anim.getCurrRegion());
        if (priorAnimKey != null && !currAnimKey.equals(priorAnimKey)) {
            Animation priorAnim = anims.get(priorAnimKey);
            if (priorAnim != null) {
                priorAnim.reset();
            }
        }
    }

    @Override
    public void reset() {
        for (Animation anim : anims.values()) {
            anim.reset();
        }
    }

}
