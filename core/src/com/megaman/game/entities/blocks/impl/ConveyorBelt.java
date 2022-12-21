package com.megaman.game.entities.blocks.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.animations.Animator;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.blocks.Block;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

public class ConveyorBelt extends Block {

    private static final float FORCE_AMOUNT = .75f;

    private static TextureRegion lLeft;
    private static TextureRegion lRight;
    private static TextureRegion rLeft;
    private static TextureRegion rRight;
    private static TextureRegion middle;

    public final Fixture forceFixture;

    public ConveyorBelt(MegamanGame game) {
        super(game);
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.CONVEYOR_BELT);
        if (lLeft == null) {
            lLeft = atlas.findRegion("LeftPart-MoveLeft");
        }
        if (lRight == null) {
            lRight = atlas.findRegion("LeftPart-MoveRight");
        }
        if (rLeft == null) {
            rLeft = atlas.findRegion("RightPart-MoveLeft");
        }
        if (rRight == null) {
            rRight = atlas.findRegion("RightPart-MoveRight");
        }
        if (middle == null) {
            middle = atlas.findRegion("MiddlePart");
        }
        forceFixture = new Fixture(this, FixtureType.FORCE);
        body.fixtures.add(forceFixture);
        ShapeHandle h = new ShapeHandle(forceFixture.bounds);
        h.colorSupplier = () -> Color.BLUE;
        putComponent(new ShapeComponent(h));
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        super.init(bounds, data);
        boolean left = (boolean) data.get(ConstKeys.LEFT);
        Rectangle forceBounds = new Rectangle().setSize(bounds.width - WorldVals.PPM / 4f, bounds.height);
        forceFixture.bounds.set(forceBounds);
        forceFixture.offset.y += WorldVals.PPM / 8f;
        Vector2 force = new Vector2(FORCE_AMOUNT * WorldVals.PPM, 0f);
        if (left) {
            force.x *= -1f;
        }
        forceFixture.putUserData(ConstKeys.VAL, force);
        Array<SpriteHandle> handles = new Array<>();
        Array<Animator> animators = new Array<>();
        int numParts = (int) (bounds.width / WorldVals.PPM);
        for (int i = 0; i < numParts; i++) {
            String part = i == 0 ? "left" : (i == numParts - 1 ? "right" : "middle");
            TextureRegion reg;
            if (part.equals("left")) {
                reg = left ? lLeft : rLeft;
            } else if (part.equals("right")) {
                reg = left ? rLeft : rRight;
            } else {
                reg = middle;
            }
            Animation anim = new Animation(reg, 2, .15f);
            Sprite sprite = new Sprite();
            sprite.setBounds(
                    bounds.x + i * WorldVals.PPM,
                    bounds.y,
                    WorldVals.PPM,
                    WorldVals.PPM);
            handles.add(new SpriteHandle(sprite));
            animators.add(new Animator(sprite, anim));
        }
        putComponent(new SpriteComponent(handles));
        putComponent(new AnimationComponent(animators));
    }

}
