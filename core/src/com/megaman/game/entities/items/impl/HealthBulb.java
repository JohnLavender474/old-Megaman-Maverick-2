package com.megaman.game.entities.items.impl;

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
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.cull.CullOnEventComponent;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.items.Item;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventType;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.TimeMarkedRunnable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

public class HealthBulb extends Entity implements Item {

    private static final Logger logger = new Logger(HealthBulb.class, MegamanGame.DEBUG && true);

    public static final int SMALL_HEALTH = 3;
    public static final int LARGE_HEALTH = 6;

    private static final float GRAV = -.15f;
    private static final float G_GRAV = -.001f;
    private static final float CLAMP_X = 1.25f;
    private static final float TIME_TO_BLINK = 2f;
    private static final float BLINK_DUR = .01f;
    private static final float CULL_DUR = 3.5f;

    private final Body body;
    private final Sprite sprite;
    private final Timer blinkTimer;
    private final Timer cullTimer;

    private Fixture itemFixture;
    private Fixture feetFixture;
    private Fixture bodyFixture;

    private boolean large;
    private boolean timeCull;
    private boolean blink;
    private boolean warning;

    public HealthBulb(MegamanGame game) {
        super(game, EntityType.ITEM);
        sprite = new Sprite();
        body = new Body(BodyType.DYNAMIC);
        blinkTimer = new Timer(BLINK_DUR);
        cullTimer = new Timer(CULL_DUR, new TimeMarkedRunnable(TIME_TO_BLINK, () -> warning = true));
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());
        putComponent(updatableComponent());
        putComponent(cullOnEventComponent());
        putComponent(new CullOutOfBoundsComponent(() -> body.bounds));
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        init(ShapeUtils.getCenterPoint(bounds), data);
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        large = (boolean) data.get(ConstKeys.LARGE);
        timeCull = !data.containsKey(ConstKeys.TIMED) || (boolean) data.get(ConstKeys.TIMED);
        warning = false;
        blink = false;
        blinkTimer.setToEnd();
        cullTimer.reset();
        body.bounds.setSize((large ? .5f : .25f) * WorldVals.PPM);
        body.bounds.setCenter(spawn);
        ((Rectangle) bodyFixture.shape).set(body.bounds);
        ((Rectangle) itemFixture.shape).set(body.bounds);
        feetFixture.offset.y = (large ? -.25f : -.125f) * WorldVals.PPM;
    }

    @Override
    public void contactWithPlayer(Megaman m) {
        logger.log("Contact with player");
        dead = true;
        game.getEventMan().submit(new Event(EventType.ADD_PLAYER_HEALTH, new ObjectMap<>() {{
            put(ConstKeys.VAL, large ? LARGE_HEALTH : SMALL_HEALTH);
        }}));
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            if (!timeCull) {
                return;
            }
            if (warning) {
                blinkTimer.update(delta);
                if (blinkTimer.isFinished()) {
                    blinkTimer.reset();
                    blink = !blink;
                }
            }
            cullTimer.update(delta);
            if (cullTimer.isFinished()) {
                dead = true;
            }
        });
    }

    private BodyComponent bodyComponent() {
        body.velClamp.x = CLAMP_X * WorldVals.PPM;
        body.affectedByResistance = true;
        Array<ShapeHandle> h = new Array<>();

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY, new Rectangle());
        body.add(bodyFixture);
        h.add(new ShapeHandle(() -> bodyFixture.shape, Color.PURPLE));
        this.bodyFixture = bodyFixture;

        // feet fixture
        Fixture feetFixture = new Fixture(this, FixtureType.FEET, new Rectangle());
        body.add(feetFixture);
        h.add(new ShapeHandle(() -> feetFixture.shape, Color.GREEN));
        this.feetFixture = feetFixture;

        // item fixture
        Fixture itemFixture = new Fixture(this, FixtureType.ITEM, new Rectangle());
        body.add(itemFixture);
        h.add(new ShapeHandle(() -> itemFixture.shape, Color.RED));
        this.itemFixture = itemFixture;

        // pre-process
        body.preProcess = delta -> {
            body.gravityOn = UtilMethods.isInCamBounds(game.getGameCam(), body.bounds);
            body.gravity.y = (body.is(BodySense.FEET_ON_GROUND) ? G_GRAV : GRAV) * WorldVals.PPM;
        };

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }

        return new BodyComponent(body);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
            h.hidden = blink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> large ? "Large" : "Small";
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ITEMS_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Large", new Animation(atlas.findRegion("HealthBulb"), 2, .15f));
            put("Small", new Animation(atlas.findRegion("SmallHealthBulb")));
        }});
    }

    private CullOnEventComponent cullOnEventComponent() {
        Set<EventType> s = EnumSet.of(
                EventType.PLAYER_JUST_DIED,
                EventType.ENTER_BOSS_ROOM);
        return new CullOnEventComponent(e -> s.contains(e.type));
    }

}
