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
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListener;
import com.megaman.game.movement.trajectory.Trajectory;
import com.megaman.game.movement.trajectory.TrajectoryComponent;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.WorldVals;

public class GearTrolley extends Block implements EventListener {

    private static final float WIDTH = 1.25f;
    private static final float HEIGHT = .35f;

    private static TextureRegion gearTrolleyReg;

    private final SpriteHandle spriteHandle;

    public GearTrolley(MegamanGame game) {
        super(game, false);
        if (gearTrolleyReg == null) {
            gearTrolleyReg = game.getAssMan().getTextureRegion(TextureAsset.PLATFORMS_1, "GearTrolleyPlatform");
        }
        spriteHandle = new SpriteHandle(new Sprite(), 2);
        putComponent(shapeComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());
        putComponent(new TrajectoryComponent());
        runOnDeath.add(() -> game.getEventMan().remove(this));
    }

    @Override
    public void init(Rectangle ignore, ObjectMap<String, Object> data) {
        game.getEventMan().add(this);
        Vector2 pos = ShapeUtils.getCenterPoint((Rectangle) data.get(ConstKeys.SPAWN));
        Rectangle bounds = new Rectangle().setSize(WIDTH * WorldVals.PPM, HEIGHT * WorldVals.PPM);
        ShapeUtils.setBottomCenterToPoint(bounds, pos);
        super.init(bounds, data);
        Rectangle gameRoom = (Rectangle) data.get(ConstKeys.ROOM);
        putComponent(new CullOutOfBoundsComponent(gameRoom));
        String trajStr = (String) data.get(ConstKeys.TRAJECTORY);
        TrajectoryComponent t = getComponent(TrajectoryComponent.class);
        t.trajectory = new Trajectory(body, trajStr);
    }

    @Override
    public void listenForEvent(Event event) {
        switch (event.type) {
            case BEGIN_ROOM_TRANS -> {
                spriteHandle.hidden = true;
                getComponent(TrajectoryComponent.class).reset();
            }
            case END_ROOM_TRANS -> spriteHandle.hidden = false;
        }
    }

    private SpriteComponent spriteComponent() {
        spriteHandle.sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        spriteHandle.updatable = delta -> {
            spriteHandle.setPosition(body.bounds, Position.CENTER);
            spriteHandle.sprite.translateY(-WorldVals.PPM / 16f);
        };
        return new SpriteComponent(spriteHandle);
    }

    private AnimationComponent animationComponent() {
        Animation anim = new Animation(gearTrolleyReg, 2, .15f);
        return new AnimationComponent(spriteHandle.sprite, anim);
    }

}
