package com.megaman.game.entities.special.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.animations.Animator;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.world.*;

public class Water extends Entity {

    private static final String WATER_REG = "Water/Water";
    private static final String UNDER_REG = "Water/Under";
    private static final String SURFACE_REG = "Water/Surface";

    private static TextureRegion waterReg;
    private static TextureRegion surfaceReg;
    private static TextureRegion underReg;

    private static final float WATER_ALPHA = .5f;

    private final Fixture water;
    private final Body body;

    public Water(MegamanGame game) {
        super(game, EntityType.SPECIAL);
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENVIRONS_1);
        if (waterReg == null) {
            waterReg = atlas.findRegion(WATER_REG);
        }
        if (underReg == null) {
            underReg = atlas.findRegion(UNDER_REG);
        }
        if (surfaceReg == null) {
            surfaceReg = atlas.findRegion(SURFACE_REG);
        }
        body = new Body(BodyType.ABSTRACT);
        water = new Fixture(this, FixtureType.WATER, new Rectangle());
        body.add(water);
        putComponent(new BodyComponent(body));
        putComponent(new CullOutOfBoundsComponent(() -> body.bounds));
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        body.bounds.set(bounds);
        ((Rectangle) water.shape).set(bounds);
        Array<SpriteHandle> handles = new Array<>();
        Array<Animator> animators = new Array<>();
        Sprite waterSprite = new Sprite(waterReg);
        waterSprite.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        waterSprite.setAlpha(WATER_ALPHA);
        handles.add(new SpriteHandle(waterSprite, 5));
        int rows = (int) (bounds.height / WorldVals.PPM);
        int cols = (int) (bounds.width / WorldVals.PPM);
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                Vector2 pos = new Vector2(bounds.x + x * WorldVals.PPM, bounds.y + y * WorldVals.PPM);
                TextureRegion region = y == rows - 1 ? surfaceReg : underReg;
                Animation anim = new Animation(region, 2, .15f);
                Sprite sprite = new Sprite();
                sprite.setBounds(pos.x, pos.y, WorldVals.PPM, WorldVals.PPM);
                sprite.setAlpha(WATER_ALPHA);
                handles.add(new SpriteHandle(sprite, -1));
                animators.add(new Animator(sprite, anim));
            }
        }
        putComponent(new SpriteComponent(handles));
        putComponent(new AnimationComponent(animators));
    }

}
