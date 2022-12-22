package com.megaman.game.entities.decorations;

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
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.WorldVals;

public class SmokePuff extends Entity {

    private static TextureRegion smokePuffReg;

    private final Vector2 pos;
    private final Sprite sprite;
    private final Animation anim;

    public SmokePuff(MegamanGame game) {
        super(game, EntityType.DECORATION);
        if (smokePuffReg == null) {
            smokePuffReg = game.getAssMan().getTextureRegion(TextureAsset.DECORATIONS, "SmokePuff");
        }
        pos = new Vector2();
        sprite = new Sprite();
        anim = new Animation(smokePuffReg, 7, .025f, false);
        putComponent(spriteComponent());
        putComponent(updatableComponent());
        putComponent(new AnimationComponent(sprite, anim));
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        pos.set(spawn);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(WorldVals.PPM, WorldVals.PPM);
        SpriteHandle handle = new SpriteHandle(sprite, 3);
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
