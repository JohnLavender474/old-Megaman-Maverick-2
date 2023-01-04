package com.megaman.game.entities.explosions.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.AssetsManager;
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

public class ChargedShotExplosion extends Entity implements Damager, Faceable {

    private static final float FULLY_CHARGED_DUR = .6f;
    private static final float HALF_CHARGED_DUR = .3f;
    private static final float SOUND_INTERVAL = .15f;

    private static TextureRegion fullyChargedReg;
    private static TextureRegion halfChargedReg;

    private final Body body;
    private final Sprite sprite;
    private final Timer soundTimer;

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
        AssetsManager assMan = game.getAssMan();
        if (fullyChargedReg == null) {
            fullyChargedReg = assMan.getTextureRegion(TextureAsset.MEGAMAN_CHARGED_SHOT, "Collide");
        }
        if (halfChargedReg == null) {
            halfChargedReg = assMan.getTextureRegion(TextureAsset.EXPLOSIONS_1, "HalfChargedShot");
        }
        this.body = new Body(BodyType.ABSTRACT);
        this.soundTimer = new Timer(SOUND_INTERVAL);
        this.sprite = new Sprite();
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());
        putComponent(new SoundComponent());
        putComponent(updatableComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        soundTimer.reset();
        owner = (Entity) data.get(ConstKeys.OWNER);
        facing = (Facing) data.get(ConstKeys.DIR);
        fullyCharged = (boolean) data.get(ConstKeys.BOOL);
        cullTimer = new Timer(fullyCharged ? FULLY_CHARGED_DUR : HALF_CHARGED_DUR);
        body.bounds.setCenter(spawn);
        float spriteDim = (fullyCharged ? 1.75f : 1.25f) * WorldVals.PPM;
        sprite.setSize(spriteDim, spriteDim);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            soundTimer.update(delta);
            if (soundTimer.isFinished()) {
                getComponent(SoundComponent.class).requestToPlay(SoundAsset.ENEMY_DAMAGE_SOUND);
                soundTimer.reset();
            }
            cullTimer.update(delta);
            if (cullTimer.isFinished()) {
                dead = true;
            }
        });
    }

    private BodyComponent bodyComponent() {
        body.bounds.setSize(WorldVals.PPM, WorldVals.PPM);
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(WorldVals.PPM));
        body.add(damagerFixture);
        return new BodyComponent(body);
    }

    private SpriteComponent spriteComponent() {
        SpriteHandle handle = new SpriteHandle(sprite, 4);
        handle.updatable = delta -> {
            handle.setPosition(body.bounds, Position.CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
        };
        return new SpriteComponent(handle);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, () -> fullyCharged ? "charged" : "half", new ObjectMap<>() {{
            put("charged", new Animation(fullyChargedReg, 3, .05f));
            put("half", new Animation(halfChargedReg, 3, .05f));
        }});
    }

}
