package com.megaman.game.entities.enemies.impl;

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
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.entities.projectiles.impl.Fireball;
import com.megaman.game.health.HealthVals;
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

public class Dragonfly extends Enemy implements Faceable {

    public enum DragonflyBehavior {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_HORIZONTAL
    }

    private static final float CULL_DUR = 2f;
    private static final float VERT_SPEED = 18f;
    private static final float HORIZ_SPEED = 14f;
    private static final float CHANGE_BEHAV_DUR = .35f;
    private static final float VERT_SCANNER_OFFSET = 2f;
    private static final float HORIZ_SCANNER_OFFSET = 3f;

    private static TextureRegion dragonFlyReg;

    private final Sprite sprite;
    private final Timer changeBehavTimer;

    private boolean toLeftBounds;
    private DragonflyBehavior currBehavior;
    private DragonflyBehavior prevBehavior;

    @Getter
    @Setter
    private Facing facing;

    public Dragonfly(MegamanGame game) {
        super(game, CULL_DUR, BodyType.ABSTRACT);
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
        currBehavior = prevBehavior = DragonflyBehavior.MOVE_UP;
        changeBehavTimer.reset();
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(5));
            put(Fireball.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShot.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShotExplosion.class, new DamageNegotiation(15));
        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.bounds.setSize(.75f * WorldVals.PPM);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(WorldVals.PPM));
        body.add(damageableFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.75f * WorldVals.PPM));
        body.add(damagerFixture);

        // megaman scanner fixture
        Fixture megamanScanner = new Fixture(this, FixtureType.CUSTOM,
                new Rectangle().setSize(32f * WorldVals.PPM, WorldVals.PPM));
        body.add(megamanScanner);

        // out-of-bounds scanner
        Fixture oobScanner = new Fixture(this, FixtureType.CUSTOM,
                new Rectangle().setSize(WorldVals.PPM / 32f));
        body.add(oobScanner);

        // pre-process
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

        // post-process
        body.postProcess = delta -> {
            if (!changeBehavTimer.isFinished()) {
                body.velocity.set(Vector2.Zero);
                return;
            }
            switch (currBehavior) {
                case MOVE_UP -> {
                    if (!UtilMethods.isInCamBounds(game.getGameCam(), (Rectangle) oobScanner.shape)) {
                        changeBehavior(DragonflyBehavior.MOVE_HORIZONTAL);
                        toLeftBounds = isMegamanLeft();
                    }
                }
                case MOVE_HORIZONTAL -> {
                    boolean doChange = (toLeftBounds && !isMegamanLeft()) || (!toLeftBounds && isMegamanLeft());
                    if (doChange && !UtilMethods.isInCamBounds(game.getGameCam(), (Rectangle) oobScanner.shape)) {
                        changeBehavior(prevBehavior == DragonflyBehavior.MOVE_UP ?
                                DragonflyBehavior.MOVE_DOWN : DragonflyBehavior.MOVE_UP);
                    }
                }
                case MOVE_DOWN -> {
                    if (megamanScanner.shape.contains(getMegamanCenter()) ||
                            (!isMegamanBelow() && !UtilMethods.isInCamBounds(
                                    game.getGameCam(), (Rectangle) oobScanner.shape))) {
                        changeBehavior(DragonflyBehavior.MOVE_HORIZONTAL);
                        toLeftBounds = isMegamanLeft();
                    }
                }
            }
        };
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.CENTER);
            if (currBehavior == DragonflyBehavior.MOVE_UP || currBehavior == DragonflyBehavior.MOVE_DOWN) {
                setFacing(isMegamanLeft() ? Facing.LEFT : Facing.RIGHT);
            }
            sprite.setFlip(is(Facing.LEFT), false);
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(dragonFlyReg, 2, .1f));
    }

    private void changeBehavior(DragonflyBehavior behavior) {
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
