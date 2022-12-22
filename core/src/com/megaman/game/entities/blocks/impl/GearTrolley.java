package com.megaman.game.entities.blocks.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.blocks.Block;
import com.megaman.game.movement.trajectory.Trajectory;
import com.megaman.game.movement.trajectory.TrajectoryComponent;
import com.megaman.game.movement.trajectory.TrajectoryParser;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.WorldVals;

public class GearTrolley extends Block {

    private static TextureRegion gearTrolleyReg;

    public static final float WIDTH = 1.25f;
    public static final float HEIGHT = .35f;

    public final Sprite sprite;

    public GearTrolley(MegamanGame game) {
        super(game, false);
        if (gearTrolleyReg == null) {
            gearTrolleyReg = game.getAssMan().getTextureRegion(TextureAsset.CUSTOM_TILES_1, "GearTrolleyPlatform");
        }
        sprite = new Sprite();
        putComponent(shapeComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle ignore, ObjectMap<String, Object> data) {
        Vector2 pos = ShapeUtils.getCenterPoint((Rectangle) data.get(ConstKeys.SPAWN));
        Rectangle bounds = new Rectangle(pos.x - (WIDTH * WorldVals.PPM / 2f),
                pos.y, WIDTH * WorldVals.PPM, HEIGHT * WorldVals.PPM);
        super.init(bounds, data);
        Rectangle gameRoom = (Rectangle) data.get(ConstKeys.ROOM);
        putComponent(new CullOutOfBoundsComponent(gameRoom));
        String trajStr = (String) data.get(ConstKeys.TRAJECTORY);
        putComponent(new TrajectoryComponent(body, trajStr));
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 2);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.CENTER);
            h.sprite.translateY(-WorldVals.PPM / 16f);
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Animation anim = new Animation(gearTrolleyReg, 2, .15f);
        return new AnimationComponent(sprite, anim);
    }

    private ShapeComponent shapeComponent() {
        return new ShapeComponent(body.bounds);
    }

}
