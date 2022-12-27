package com.megaman.game.entities.sensors.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventListenerComponent;
import com.megaman.game.events.EventType;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;

import java.util.function.Supplier;

public class Gate extends Entity implements Resettable {

    private static final Logger logger = new Logger(Gate.class, MegamanGame.DEBUG && true);

    public enum GateState {
        OPENABLE,
        OPENING,
        OPEN,
        CLOSING,
        CLOSED
    }

    private static final float DUR = .5f;

    private final Body body;
    private final Timer timer;
    private final Sprite sprite;

    private GateState state;
    private String nextRoom;
    private boolean transFinished;

    public Gate(MegamanGame game) {
        super(game, EntityType.INTERACTIVE);
        body = new Body(BodyType.ABSTRACT);
        timer = new Timer(DUR, true);
        sprite = new Sprite();
        putComponent(bodyComponent());
        putComponent(eventListenerComponent());
        putComponent(updatableComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());
        putComponent(new SoundComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        reset();
        Vector2 spawn = ShapeUtils.getCenterPoint(bounds);
        body.bounds.setCenter(spawn);
        nextRoom = (String) data.get(ConstKeys.ROOM);
    }

    @Override
    public void reset() {
        timer.reset();
        transFinished = false;
        state = GateState.OPENABLE;
    }

    public boolean isState(GateState state) {
        return this.state == state;
    }

    public void trigger() {
        logger.log("Set gate state OPENING");
        state = GateState.OPENING;
        game.getAudioMan().playSound(SoundAsset.BOSS_DOOR);
        game.getEventMan().dispatchEvent(new Event(EventType.GATE_INIT_OPENING));
    }

    private BodyComponent bodyComponent() {
        body.bounds.setSize(2f * WorldVals.PPM, 3f * WorldVals.PPM);
        Fixture gateFixture = new Fixture(this, FixtureType.GATE, new Rectangle(body.bounds));
        body.fixtures.add(gateFixture);
        return new BodyComponent(body);
    }

    private EventListenerComponent eventListenerComponent() {
        return new EventListenerComponent(e -> {
            switch (e.eventType) {
                case PLAYER_SPAWN -> reset();
                case END_GAME_ROOM_TRANS -> {
                    if (nextRoom.equals(e.getInfo(ConstKeys.ROOM, RectangleMapObject.class).getName())) {
                        transFinished = true;
                    }
                }
            }
        });
    }

    private UpdatableComponent updatableComponent() {
        UpdatableComponent c = new UpdatableComponent();
        c.add(delta -> {
            timer.update(delta);
            if (timer.isFinished()) {
                logger.log("Set gate state OPEN");
                timer.reset();
                state = GateState.OPEN;
                game.getEventMan().dispatchEvent(new Event(EventType.GATE_FINISH_OPENING));
                game.getEventMan().dispatchEvent(new Event(EventType.NEXT_GAME_ROOM_REQ, new ObjectMap<>() {{
                    put(ConstKeys.ROOM, nextRoom);
                }}));
            }
        }, () -> state == GateState.OPENING);
        c.add(delta -> {
            if (transFinished) {
                logger.log("Set gate state CLOSING");
                transFinished = false;
                state = GateState.CLOSING;
                game.getAudioMan().playSound(SoundAsset.BOSS_DOOR);
                game.getEventMan().dispatchEvent(new Event(EventType.GATE_INIT_CLOSING));
            }
        }, () -> state == GateState.OPEN);
        c.add(delta -> {
            timer.update(delta);
            if (timer.isFinished()) {
                logger.log("Set gate state CLOSED");
                timer.reset();
                state = GateState.CLOSED;
                game.getEventMan().dispatchEvent(new Event(EventType.GATE_FINISH_CLOSING));
            }
        }, () -> state == GateState.CLOSING);
        return c;
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(4f * WorldVals.PPM, 3f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 3);
        h.updatable = delta -> {
            h.setPosition(body.bounds, state == GateState.CLOSING || state == GateState.CLOSED ?
                    Position.BOTTOM_RIGHT : Position.BOTTOM_LEFT);
            h.hidden = state == GateState.OPEN;
            sprite.setFlip(state == GateState.CLOSING || state == GateState.CLOSED, false);
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.DOORS);
        Supplier<String> keySupplier = () -> switch (state) {
            case OPENABLE, CLOSED -> "closed";
            case OPENING -> "opening";
            case CLOSING -> "closing";
            case OPEN -> null;
        };
        Animation closed = new Animation(atlas.findRegion("closed"));
        Animation opening = new Animation(atlas.findRegion("opening"), 4, .125f, false);
        Animation closing = new Animation(opening, true);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("closed", closed);
            put("opening", opening);
            put("closing", closing);
        }});
    }

}
