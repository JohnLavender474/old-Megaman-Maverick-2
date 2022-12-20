package com.megaman.game.animations;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.Component;

import java.util.function.Function;
import java.util.function.Supplier;

public class AnimationComponent implements Component {

    public final Array<Animator> animators;

    public AnimationComponent(Sprite sprite, Animation anim) {
        this(new Animator(sprite, anim));
    }

    public AnimationComponent(Sprite sprite, Supplier<String> keySupplier, Function<String, Animation> animFunc) {
        this(new Animator(sprite, keySupplier, animFunc));
    }

    public AnimationComponent(Animator animator) {
        this.animators = new Array<>();
        this.animators.add(animator);
    }

    public AnimationComponent(Array<Animator> animators) {
        this.animators = animators;
    }

}
