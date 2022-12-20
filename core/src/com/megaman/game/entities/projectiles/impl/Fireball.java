package com.megaman.game.entities.projectiles.impl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.audio.SoundRequest;
import com.megaman.game.components.ComponentType;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.function.Supplier;

@Getter
@Setter
public class Fireball extends Projectile {

    private final Timer burnTimer = new Timer(1.5f);

    private float rotation;
    private boolean isLanded;
    private boolean wasLanded;

    public Fireball(MegamanGame game, Entity owner) {
        super(game, owner, .25f);
        addComponent(spriteComponent());
        addComponent(animationComponent());
        addComponent(updatableComponent());
        addComponent(bodyComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {

    }

    /*
    @Override
    public void hit(Fixture fixture) {
        if (fixture.isAnyFixtureType(BLOCK, DAMAGEABLE, SHIELD)) {
            setLanded(true);
        }
    }
     */

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            if (!wasLanded && isLanded) {
                ((SoundComponent) getComponent(ComponentType.SOUND))
                        .sReqs.add(new SoundRequest(SoundAsset.ATOMIC_FIRE_SOUND, false));
            }
            wasLanded = isLanded;
            if (isLanded) {
                burnTimer.update(delta);
            }
            dead = burnTimer.isFinished();
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.25f * WorldConstVals.PPM, 1.25f * WorldConstVals.PPM);
        SpriteHandle handle = new SpriteHandle(sprite);
        handle.priority = 3;
        handle.runnable = () -> {
            ShapeUtils.setToPoint(sprite.getBoundingRectangle(),
                    ShapeUtils.getBottomCenterPoint(body.bounds), Position.BOTTOM_CENTER);
            sprite.setRotation(isLanded ? 0f : rotation);
        };
        return new SpriteComponent(handle);
    }

    private AnimationComponent animationComponent() {
        TextureAtlas textureAtlas = game.getAssMan().getAsset(
                TextureAsset.FIRE.getSrc(), TextureAtlas.class);
        Supplier<String> keySupplier = () -> isLanded() ? "Flame" : "Fireball";
        Map<String, Animation> timedAnimations = Map.of(
                "Flame", new Animation(textureAtlas.findRegion("Flame"), 4, .1f),
                "Fireball", new Animation(textureAtlas.findRegion("Fireball")));
        return new AnimationComponent(sprite, keySupplier, timedAnimations::get);
    }

    private BodyComponent bodyComponent() {
        body.bounds.setSize(WorldConstVals.PPM, WorldConstVals.PPM);
        body.gravity.y = -WorldConstVals.PPM * .35f;
        // projectile fixture
        Fixture projectileFixture = new Fixture(this, FixtureType.DAMAGER, .85f * WorldConstVals.PPM);
        body.fixtures.add(projectileFixture);
        return new BodyComponent(body);
    }

}
