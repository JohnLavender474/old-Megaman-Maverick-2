package com.megaman.game.entities.explosions.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
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
import com.megaman.game.world.WorldVals;

public class ExplosionOrb extends Entity {

    private static TextureRegion explosionOrbReg;

    private final Sprite sprite = new Sprite();
    private final Vector2 traj = new Vector2();

    public ExplosionOrb(MegamanGame game) {
        super(game, EntityType.EXPLOSION);
        if (explosionOrbReg == null) {
            explosionOrbReg = game.getAssMan().getTextureRegion(TextureAsset.DECORATIONS, "PlayerExplosionOrbs");
        }
        putComponent(spriteComponent());
        putComponent(animationComponent());
        putComponent(new CullOutOfBoundsComponent(sprite::getBoundingRectangle));
        putComponent(new CullOnEventComponent(e -> e.eventType == EventType.PLAYER_SPAWN));
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        sprite.setCenter(spawn.x, spawn.y);
        traj.set((Vector2) data.get(ConstKeys.TRAJECTORY));
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(3f * WorldVals.PPM, 3f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> sprite.translate(traj.x * WorldVals.PPM * delta, traj.y * WorldVals.PPM * delta);
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(explosionOrbReg, 2, .075f));
    }

}
