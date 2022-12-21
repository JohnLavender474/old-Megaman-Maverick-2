package com.megaman.game.entities.explosions.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.entities.*;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;
import java.util.function.Supplier;

public class ChargedShotExplosion extends Entity implements Damager, Faceable {

    private static final float FULLY_CHARGED_DUR = .75f;
    private static final float HALF_CHARGED_DUR = .15f;

    private final Body body = new Body(BodyType.ABSTRACT);
    private final Timer soundTimer = new Timer(.15f);
    private final Sprite sprite = new Sprite();

    @Getter
    private Entity owner;
    private Timer cullTimer;
    @Getter
    private boolean fullyCharged;
    @Getter
    @Setter
    private Facing facing;

    public ChargedShotExplosion(MegamanGame game) {
        super(game, EntityType.EXPLOSION);
        addComponent(bodyComponent());
        addComponent(spriteComponent());
        addComponent(animationComponent());
        addComponent(new SoundComponent());
        addComponent(updatableComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        soundTimer.reset();
        // owner
        owner = (Entity) data.get(ConstKeys.OWNER);
        // facing
        facing = (Facing) data.get(ConstKeys.DIR);
        // fully charged?
        fullyCharged = (boolean) data.get(ConstKeys.BOOL);
        cullTimer = new Timer(fullyCharged ? FULLY_CHARGED_DUR : HALF_CHARGED_DUR);
        // body
        body.bounds.setCenter(spawn);
        // sprite
        float spriteDim = (fullyCharged ? 1.75f : 1.25f) * WorldVals.PPM;
        sprite.setSize(spriteDim, spriteDim);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            soundTimer.update(delta);
            if (soundTimer.isJustFinished()) {
                getComponent(SoundComponent.class).request(SoundAsset.ENEMY_DAMAGE_SOUND);
                if (fullyCharged) {
                    soundTimer.reset();
                }
            }
            cullTimer.update(delta);
            if (cullTimer.isFinished()) {
                dead = true;
            }
        });
    }

    private BodyComponent bodyComponent() {
        body.bounds.setSize(WorldVals.PPM, WorldVals.PPM);
        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, WorldVals.PPM);
        body.fixtures.add(damagerFixture);
        return new BodyComponent(body);
    }

    private SpriteComponent spriteComponent() {
        SpriteHandle handle = new SpriteHandle(sprite, 5);
        handle.runnable = () -> {
            handle.setPosition(body.bounds, Position.CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
        };
        return new SpriteComponent(handle);
    }

    private AnimationComponent animationComponent() {
        TextureRegion fullyChargedRegion = game.getAssMan()
                .getTextureRegion(TextureAsset.MEGAMAN_CHARGED_SHOT, "Collide");
        TextureRegion halfChargedRegion = game.getAssMan()
                .getTextureRegion(TextureAsset.MEGAMAN_HALF_CHARGED_SHOT, "Collide");
        Animation fullyChargedAnim = new Animation(fullyChargedRegion, 3, .05f);
        Animation halfChargedAnim = new Animation(halfChargedRegion, 3, .05f, false);
        Supplier<String> keySupplier = () -> fullyCharged ? "FullyCharged" : "HalfCharged";
        Function<String, Animation> func = key -> key.equals("FullyCharged") ? fullyChargedAnim : halfChargedAnim;
        return new AnimationComponent(sprite, keySupplier, func);
    }

}
