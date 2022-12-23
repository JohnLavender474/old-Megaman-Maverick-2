package com.megaman.game.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Faceable;
import com.megaman.game.entities.Facing;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class DragonFly extends Enemy implements Faceable {

    public enum DragonFlyBehavior {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_HORIZONTAL
    }

    private static final float VERT_SPEED = 18f;
    private static final float HORIZ_SPEED = 14f;
    private static final float VERT_SCANNER_OFFSET = 2f;
    private static final float HORIZ_SCANNER_OFFSET = 3f;
    private static final float CHANGE_BEHAV_DUR = .35f;

    private static TextureRegion dragonFlyReg;

    private final Sprite sprite;
    private final Timer changeBehavTimer;

    private boolean toLeftBounds;
    private DragonFlyBehavior currBehavior;
    private DragonFlyBehavior prevBehavior;

    @Getter
    @Setter
    private Facing facing;

    public DragonFly(MegamanGame game) {
        super(game, BodyType.ABSTRACT);
        if (dragonFlyReg == null) {
            dragonFlyReg = game.getAssMan().getTextureRegion(TextureAsset.ENEMIES_1, "Dragonfly");
        }
        sprite = new Sprite();
        changeBehavTimer = new Timer(CHANGE_BEHAV_DUR);
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 center = ShapeUtils.getCenterPoint(bounds);
        body.bounds.setCenter(center);
        currBehavior = prevBehavior = DragonFlyBehavior.MOVE_UP;
        changeBehavTimer.reset();
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.bounds.setSize(.75f * WorldVals.PPM);
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(WorldVals.PPM));
        body.fixtures.add(damageableFixture);
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.75f * WorldVals.PPM));
        body.fixtures.add(damagerFixture);
        Fixture megamanScanner = new Fixture(this, FixtureType.CUSTOM,
                new Rectangle().setSize(32f * WorldVals.PPM, WorldVals.PPM));
        body.fixtures.add(megamanScanner);
        Fixture oobScanner = new Fixture(this, FixtureType.CUSTOM,
                new Rectangle().setSize(WorldVals.PPM / 32f));
        body.fixtures.add(oobScanner);
        body.preProcess = delta -> {
            changeBehavTimer.update(delta);
            if (!changeBehavTimer.isFinished()) {
                body.velocity.set(Vector2.Zero);
                return;
            }
            switch (currBehavior) {
                case MOVE_UP -> {
                    body.velocity.set(0f, VERT_SPEED * WorldVals.PPM);
                    oobScanner.offset.set(0f, VERT_SCANNER_OFFSET * WorldVals.PPM);
                }
                case MOVE_HORIZONTAL -> {
                    float xVel = HORIZ_SPEED * WorldVals.PPM;
                    if (toLeftBounds) {
                        xVel *= -1f;
                    }
                    body.velocity.set(xVel, 0f);
                    float xOffset = HORIZ_SCANNER_OFFSET * WorldVals.PPM;
                    if (toLeftBounds) {
                        xOffset *= -1f;
                    }
                    oobScanner.offset.set(xOffset, 0f);
                }
                case MOVE_DOWN -> {
                    body.velocity.set(0f, -VERT_SPEED * WorldVals.PPM);
                    oobScanner.offset.set(0f, -VERT_SCANNER_OFFSET * WorldVals.PPM);
                }
            }
        };
        body.postProcess = delta -> {
            if (!changeBehavTimer.isFinished()) {
                body.velocity.set(Vector2.Zero);
                return;
            }
            switch (currBehavior) {
                case MOVE_UP -> {
                    if (!UtilMethods.isInCamBounds(game.getGameCam(), (Rectangle) oobScanner.shape)) {
                        changeBehavior(DragonFlyBehavior.MOVE_HORIZONTAL);
                        toLeftBounds = isMegamanLeft();
                    }
                }
                case MOVE_HORIZONTAL -> {
                    boolean doChange = (toLeftBounds && !isMegamanLeft()) || (!toLeftBounds && isMegamanLeft());
                    if (doChange && !UtilMethods.isInCamBounds(game.getGameCam(), (Rectangle) oobScanner.shape)) {
                        changeBehavior(prevBehavior == DragonFlyBehavior.MOVE_UP ?
                                DragonFlyBehavior.MOVE_DOWN : DragonFlyBehavior.MOVE_UP);
                    }
                }
                case MOVE_DOWN -> {
                    if (megamanScanner.shape.contains(getMegamanCenter()) ||
                            (!isMegamanBelow() && !UtilMethods.isInCamBounds(
                                    game.getGameCam(), (Rectangle) oobScanner.shape))) {
                        changeBehavior(DragonFlyBehavior.MOVE_HORIZONTAL);
                        toLeftBounds = isMegamanLeft();
                    }
                }
            }
        };
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 2);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.CENTER);
            if (currBehavior == DragonFlyBehavior.MOVE_UP || currBehavior == DragonFlyBehavior.MOVE_DOWN) {
                setFacing(isMegamanLeft() ? Facing.LEFT : Facing.RIGHT);
            }
            sprite.setFlip(is(Facing.LEFT), false);
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(dragonFlyReg, 2, .1f));
    }

    private void changeBehavior(DragonFlyBehavior behavior) {
        changeBehavTimer.reset();
        prevBehavior = currBehavior;
        currBehavior = behavior;
    }

    private boolean isMegamanLeft() {
        float megamanX = getMegamanCenter().x;
        return body.bounds.x > megamanX;
    }

    private boolean isMegamanBelow() {
        float megamanY = getMegamanCenter().y;
        return body.bounds.y > megamanY;
    }

    private Vector2 getMegamanCenter() {
        return ShapeUtils.getCenterPoint(game.getMegaman().getComponent(BodyComponent.class).body.bounds);
    }

}
