package com.megaman.game.entities.explosions.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Body;
import com.megaman.game.world.BodyComponent;
import com.megaman.game.world.BodyType;
import com.megaman.game.world.WorldVals;

public class Explosion extends Entity {

    private static final float DUR = .275f;

    private static TextureRegion explosionReg;

    private final Body body;
    private final Sprite sprite;
    private final Timer durTimer;

    public Explosion(MegamanGame game) {
        super(game, EntityType.EXPLOSION);
        if (explosionReg == null) {
            explosionReg = game.getAssMan().getTextureRegion(TextureAsset.DECORATIONS, "Explosion");
        }
        sprite = new Sprite();
        durTimer = new Timer(DUR);
        body = new Body(BodyType.ABSTRACT);
        putComponent(bodyComponent());
        putComponent(updatableComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        body.bounds.setCenter(spawn);
        durTimer.reset();
    }

    private BodyComponent bodyComponent() {
        body.bounds.setSize(WorldVals.PPM);
        return new BodyComponent(body);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            durTimer.update(delta);
            if (durTimer.isFinished()) {
                dead = true;
            }
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(2.5f * WorldVals.PPM, 2.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> h.setPosition(body.bounds, Position.CENTER);
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(explosionReg, 11, .025f, false));
    }

}
