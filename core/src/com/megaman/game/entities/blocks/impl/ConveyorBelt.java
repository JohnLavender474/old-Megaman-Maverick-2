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
import com.megaman.game.utils.interfaces.UpdateFunc;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

public class ConveyorBelt extends Block {

    private static final float FORCE_AMOUNT = 45f;

    private static TextureRegion lLeft;
    private static TextureRegion lRight;
    private static TextureRegion rLeft;
    private static TextureRegion rRight;
    private static TextureRegion middle;

    public final Fixture forceFixture;

    public ConveyorBelt(MegamanGame game) {
        super(game, true);
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.PLATFORMS_1);
        if (lLeft == null) {
            lLeft = atlas.findRegion("ConveyorBelt/LeftPart-MoveLeft");
        }
        if (lRight == null) {
            lRight = atlas.findRegion("ConveyorBelt/LeftPart-MoveRight");
        }
        if (rLeft == null) {
            rLeft = atlas.findRegion("ConveyorBelt/RightPart-MoveLeft");
        }
        if (rRight == null) {
            rRight = atlas.findRegion("ConveyorBelt/RightPart-MoveRight");
        }
        if (middle == null) {
            middle = atlas.findRegion("ConveyorBelt/MiddlePart");
        }
        forceFixture = new Fixture(this, FixtureType.FORCE, new Rectangle());
        forceFixture.offset.y = WorldVals.PPM / 8f;
        body.add(forceFixture);
        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(new ShapeHandle(forceFixture.shape, Color.BLUE)));
        }
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        super.init(bounds, data);
        boolean left = (boolean) data.get(ConstKeys.LEFT);
        ((Rectangle) forceFixture.shape).setSize(bounds.width - WorldVals.PPM / 4f, bounds.height);

        // TODO: test

        /*
        Function<Fixture, Vector2> forceFunc = f -> force;
         */

        /*
        Function<Float, Vector2> forceFunc = delta -> force.cpy().scl(delta);
        */

        UpdateFunc<Fixture, Vector2> forceFunc = (f, delta) -> {
            float x = FORCE_AMOUNT * WorldVals.PPM * delta;
            if (left) {
                x *= -1f;
            }
            return new Vector2(x, 0f);
        };

        forceFixture.putUserData(ConstKeys.FUNCTION, forceFunc);

        // sprite anims
        Array<SpriteHandle> handles = new Array<>();
        Array<Animator> animators = new Array<>();
        int numParts = (int) (bounds.width / WorldVals.PPM);
        for (int i = 0; i < numParts; i++) {
            String part = i == 0 ? "left" : (i == numParts - 1 ? "right" : "middle");
            TextureRegion reg;
            if (part.equals("left")) {
                reg = left ? lLeft : lRight;
            } else if (part.equals("right")) {
                reg = left ? rLeft : rRight;
            } else {
                reg = middle;
            }
            Animation anim = new Animation(reg, 2, .15f);
            Sprite sprite = new Sprite();
            sprite.setBounds(bounds.x + i * WorldVals.PPM, bounds.y, WorldVals.PPM, WorldVals.PPM);
            handles.add(new SpriteHandle(sprite));
            animators.add(new Animator(sprite, anim));
        }
        putComponent(new SpriteComponent(handles));
        putComponent(new AnimationComponent(animators));
    }

}
