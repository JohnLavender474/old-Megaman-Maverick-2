package com.megaman.game.entities.items.impl;

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
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.items.Item;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.megaman.upgrades.MegaHeartTank;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventType;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.*;

public class HeartTank extends Entity implements Item {

    private static TextureRegion heartTankReg;

    private final Body body;
    private final Sprite sprite;

    private MegaHeartTank heartTank;

    public HeartTank(MegamanGame game) {
        super(game, EntityType.ITEM);
        if (heartTankReg == null) {
            heartTankReg = game.getAssMan().getTextureRegion(TextureAsset.ITEMS, "HeartTank");
        }
        sprite = new Sprite();
        body = new Body(BodyType.ABSTRACT);
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        init(ShapeUtils.getBottomCenterPoint(bounds), data);
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
        heartTank = MegaHeartTank.get((String) data.get(ConstKeys.VAL));
    }

    @Override
    public void contactWithPlayer(Megaman m) {
        dead = true;
        game.getEventMan().submit(new Event(EventType.ADD_HEART_TANK, new ObjectMap<>() {{
            put(ConstKeys.VAL, heartTank);
        }}));
    }

    private BodyComponent bodyComponent() {
        body.bounds.setSize(WorldVals.PPM);
        Fixture itemFixture = new Fixture(this, FixtureType.ITEM, new Rectangle().setSize(WorldVals.PPM));
        body.add(itemFixture);
        return new BodyComponent(body);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 1);
        h.updatable = delta -> h.setPosition(body.bounds, Position.BOTTOM_CENTER);
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(heartTankReg, 2, .15f));
    }

}
