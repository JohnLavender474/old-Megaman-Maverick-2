package com.megaman.game.entities.impl.decorations.impl;

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
import com.megaman.game.entities.impl.decorations.DecorationFactory;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.Body;
import com.megaman.game.world.WorldVals;

public class Splash extends Entity {

    private static final String SPLASH_REG = "Water/Splash";
    private static final float ALPHA = .5f;

    private static TextureRegion splashReg;

    private final Vector2 pos;
    private final Sprite sprite;
    private final Animation anim;

    public Splash(MegamanGame game) {
        super(game, EntityType.DECORATION);
        if (splashReg == null) {
            splashReg = game.getAssMan().getTextureRegion(TextureAsset.ENVIRONS_1, SPLASH_REG);
        }
        pos = new Vector2();
        sprite = new Sprite();
        anim = new Animation(splashReg, 5, .075f, false);
        putComponent(spriteComponent());
        putComponent(updatableComponent());
        putComponent(new AnimationComponent(sprite, anim));
    }

    public static void generate(MegamanGame game, Body splasher, Body water) {
        int numSplashes = (int) Math.ceil(splasher.bounds.width / WorldVals.PPM);
        for (int i = 0; i < numSplashes; i++) {
            Vector2 pos = new Vector2(
                    splasher.bounds.x + (WorldVals.PPM / 2f) + i * WorldVals.PPM,
                    water.bounds.y + water.bounds.height);
            Splash s = (Splash) game.getEntityFactories().fetch(EntityType.DECORATION, DecorationFactory.SPLASH);
            game.getGameEngine().spawn(s, pos);
        }
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        pos.set(spawn);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(WorldVals.PPM, WorldVals.PPM);
        sprite.setAlpha(ALPHA);
        SpriteHandle handle = new SpriteHandle(sprite, -1);
        handle.updatable = delta -> handle.setPosition(pos, Position.BOTTOM_CENTER);
        return new SpriteComponent(handle);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            if (anim.isFinished()) {
                dead = true;
            }
        });
    }

}
