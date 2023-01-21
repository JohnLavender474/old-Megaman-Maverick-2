package com.megaman.game.animations;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.Component;

import java.util.function.Supplier;

public class AnimationComponent implements Component {

    public final Array<Animator> animators;

    public AnimationComponent(Sprite sprite, Animation anim) {
        this(new Animator(sprite, anim));
    }

    public AnimationComponent(Sprite sprite, Supplier<String> keySupplier, ObjectMap<String, Animation> anims) {
        this(new Animator(sprite, keySupplier, anims));
    }

    public AnimationComponent(Animator animator) {
        this.animators = new Array<>();
        this.animators.add(animator);
    }

    public AnimationComponent(Animator... animators) {
        this(new Array<>(animators));
    }

    public AnimationComponent(Array<Animator> animators) {
        this.animators = animators;
    }

    @Override
    public void reset() {
        for (Animator a : animators) {
            a.reset();
        }
    }

}
