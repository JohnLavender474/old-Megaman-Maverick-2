package com.megaman.game.entities.impl.special.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.controllers.ControllerManager;
import com.megaman.game.controllers.CtrlBtn;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.utils.bounce.BounceAction;
import com.megaman.game.entities.utils.bounce.BounceDef;
import com.megaman.game.entities.utils.bounce.Bouncer;
import com.megaman.game.entities.impl.megaman.Megaman;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;

import java.util.function.Supplier;

public class SpringBouncer extends Entity implements Bouncer {

    private static final float BOUNCE_DURATION = .5f;
    private static final float X_BOUNCE = 25f;
    private static final float Y_BOUNCE = 18f;
    private static final float SPRITE_DIM = 1.5f;

    private final Body body;
    private final Sprite sprite;
    private final Timer bounceTimer;

    @Getter
    private Direction dir;
    private Fixture bounceFixture;

    public SpringBouncer(MegamanGame game) {
        super(game, EntityType.SPECIAL);
        sprite = new Sprite();
        body = new Body(BodyType.STATIC);
        bounceTimer = new Timer(BOUNCE_DURATION);
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(updatableComponent());
        putComponent(animationComponent());
        putComponent(new SoundComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        bounceTimer.setToEnd();
        body.bounds.set(bounds);
        ((Rectangle) bounceFixture.shape).set(bounds);
        String dirStr = (String) data.get(ConstKeys.DIR);
        dir = switch (dirStr) {
            case ConstKeys.UP -> Direction.UP;
            case ConstKeys.DOWN -> Direction.DOWN;
            case ConstKeys.LEFT -> Direction.LEFT;
            case ConstKeys.RIGHT -> Direction.RIGHT;
            default -> throw new IllegalArgumentException("Incompatible dir: " + dirStr);
        };
    }

    @Override
    public BounceDef bounce(Fixture f) {
        getComponent(SoundComponent.class).requestToPlay(SoundAsset.DINK_SOUND);
        bounceTimer.reset();
        Vector2 bounce = new Vector2();
        switch (dir) {
            case UP -> bounce.y = Y_BOUNCE * WorldVals.PPM;
            case DOWN -> bounce.y = -Y_BOUNCE * WorldVals.PPM;
            case LEFT -> bounce.x = -X_BOUNCE * WorldVals.PPM;
            case RIGHT -> bounce.x = X_BOUNCE * WorldVals.PPM;
        }
        if (f.entity instanceof Megaman) {
            ControllerManager ctrlMan = game.getCtrlMan();
            if ((dir == Direction.UP && ctrlMan.isPressed(CtrlBtn.DPAD_UP)) ||
                    (dir == Direction.DOWN && ctrlMan.isPressed(CtrlBtn.DPAD_DOWN))) {
                bounce.y *= 2f;
            } else if ((dir == Direction.LEFT && ctrlMan.isPressed(CtrlBtn.DPAD_LEFT)) ||
                    (dir == Direction.RIGHT && ctrlMan.isPressed(CtrlBtn.DPAD_RIGHT))) {
                bounce.x *= 2f;
            }
        }
        return new BounceDef(bounce, BounceAction.SET, BounceAction.SET);

    }

    private BodyComponent bodyComponent() {
        Array<ShapeHandle> h = new Array<>();

        Fixture bouncerFixture = new Fixture(this, FixtureType.BOUNCER, new Rectangle());
        h.add(new ShapeHandle(bouncerFixture.shape, Color.BLUE));
        body.add(bouncerFixture);
        this.bounceFixture = bouncerFixture;

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }

        return new BodyComponent(body);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(bounceTimer::update);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(SPRITE_DIM * WorldVals.PPM, SPRITE_DIM * WorldVals.PPM);
        sprite.setOrigin(SPRITE_DIM * WorldVals.PPM / 2f, SPRITE_DIM * WorldVals.PPM / 2f);
        SpriteHandle h = new SpriteHandle(sprite, 1);
        h.updatable = delta -> {
            sprite.setRotation(switch (dir) {
                case UP -> 0f;
                case DOWN -> 180f;
                case LEFT -> 90f;
                case RIGHT -> 270f;
            });
            h.setPosition(body.bounds, switch (dir) {
                case UP -> Position.BOTTOM_CENTER;
                case DOWN -> Position.TOP_CENTER;
                case LEFT -> Position.CENTER_RIGHT;
                case RIGHT -> Position.CENTER_LEFT;
            });
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> bounceTimer.isFinished() ? "Still" : "Bounce";
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.SPECIALS_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Still", new Animation(atlas.findRegion("SpringBounceStill")));
            put("Bounce", new Animation(atlas.findRegion("SpringBounce"), 5, .05f));
        }});
    }

}
