package com.megaman.game.entities.explosions.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.animations.Animator;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Body;
import com.megaman.game.world.BodyType;
import com.megaman.game.world.WorldVals;
import lombok.Getter;
import lombok.Setter;

import static com.megaman.game.assets.TextureAsset.EXPLOSIONS_1;

@Getter
@Setter
public class Disintegration extends Entity {

    public static final float CULL_DUR = .1f;

    private final Body body;
    private final Sprite sprite;
    private final Timer cullTimer;

    public Disintegration(MegamanGame game) {
        super(game, EntityType.EXPLOSION);
        sprite = new Sprite();
        cullTimer = new Timer(CULL_DUR);
        body = new Body(BodyType.ABSTRACT);
        putComponent(spriteComponent());
        putComponent(updatableComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        cullTimer.reset();
        sprite.setCenter(spawn.x, spawn.y);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(WorldVals.PPM, WorldVals.PPM);
        return new SpriteComponent(new SpriteHandle(sprite, 4));
    }

    private AnimationComponent animationComponent() {
        Animation anim = new Animation(game.getAssMan().getTextureRegion(EXPLOSIONS_1, "Disintegration"), 3, 0.005f);
        Animator animator = new Animator(sprite, anim);
        return new AnimationComponent(animator);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            cullTimer.update(delta);
            if (cullTimer.isFinished()) {
                dead = true;
            }
        });
    }

}
