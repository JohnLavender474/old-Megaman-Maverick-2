package com.megaman.game.entities.explosions.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.animations.Animator;
import com.megaman.game.Component;
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

import java.util.HashMap;
import java.util.Map;

import static com.megaman.game.assets.TextureAsset.DECORATIONS;

@Getter
@Setter
public class Disintegration extends Entity {

    public static final float DISINTEGRATION_DURATION = .1f;

    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    private final Timer timer = new Timer(DISINTEGRATION_DURATION);

    private final Body body = new Body(BodyType.ABSTRACT);
    private final Sprite sprite = new Sprite();

    public Disintegration(MegamanGame game) {
        super(game, EntityType.EXPLOSION);
        addComponent(spriteComponent());
        addComponent(updatableComponent());
        addComponent(animationComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        sprite.setCenter(spawn.x, spawn.y);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(WorldVals.PPM, WorldVals.PPM);
        return new SpriteComponent(new SpriteHandle(sprite, 4));
    }

    private AnimationComponent animationComponent() {
        Animation anim = new Animation(game.getAssMan().getTextureRegion(DECORATIONS, "Disintegration"), 3, 0.005f);
        Animator animator = new Animator(sprite, () -> "Disintegration", key -> anim);
        return new AnimationComponent(animator);
    }

    private UpdatableComponent updatableComponent() {
        UpdatableComponent c = new UpdatableComponent();
        c.add(delta -> {
            timer.update(delta);
            if (timer.isFinished()) {
                dead = true;
            }
        });
        return c;
    }

}
