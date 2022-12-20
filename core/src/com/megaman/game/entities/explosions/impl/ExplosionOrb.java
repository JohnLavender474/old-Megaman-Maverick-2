package com.megaman.game.entities.explosions.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.cull.CullOnEventComponent;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.events.EventType;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.world.WorldConstVals;

public class ExplosionOrb extends Entity {

    private final Sprite sprite = new Sprite();
    private final Vector2 traj = new Vector2();

    public ExplosionOrb(MegamanGame game) {
        super(game, EntityType.EXPLOSION);
        addComponent(spriteComponent());
        addComponent(animationComponent());
        addComponent(updatableComponent());
        addComponent(new CullOutOfBoundsComponent(sprite::getBoundingRectangle));
        addComponent(new CullOnEventComponent(e -> e.eventType == EventType.PLAYER_SPAWN));
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        sprite.setCenter(spawn.x, spawn.y);
        traj.set((Vector2) data.get(ConstKeys.TRAJECTORY));
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta ->
                sprite.translate(traj.x * WorldConstVals.PPM * delta, traj.y * WorldConstVals.PPM * delta));
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(3f * WorldConstVals.PPM, 3f * WorldConstVals.PPM);
        return new SpriteComponent(new SpriteHandle(sprite, 4));
    }

    private AnimationComponent animationComponent() {
        TextureRegion region = game.getAssMan().getAsset(TextureAsset.DECORATIONS.getSrc(), TextureAtlas.class)
                .findRegion("PlayerExplosionOrbs");
        return new AnimationComponent(sprite, new Animation(region, 2, .075f));
    }

}
