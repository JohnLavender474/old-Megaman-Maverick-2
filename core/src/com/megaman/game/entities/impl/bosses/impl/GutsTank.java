package com.megaman.game.entities.impl.bosses.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.impl.blocks.Block;
import com.megaman.game.entities.impl.bosses.Boss;
import com.megaman.game.entities.utils.damage.DamageNegotiation;
import com.megaman.game.entities.utils.damage.Damager;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GutsTank extends Boss {

    // TODO: Hand is separate from main boss

    // size = 10w x 8h
    // move 10 tiles in 8 secs
    private static final float SHOOT_DUR = 1f;
    private static final float VEL_X = 1.25f;
    private static final float WIDTH = 10f;
    private static final float HEIGHT = 8f;
    private static final float FIST_WIDTH = 1f;
    private static final float FIST_HEIGHT = 1f;
    private static final float MOVE_FIST_DUR = 1f;
    private static final float FIST_UP_Y_OFFSET = 1f;
    private static final float FIST_DOWN_Y_OFFSET = 1f;

    private static final float TEMP_MOVE_DUR = 2f;
    private static final float TEMP_MOVE_DIST = .5f;

    private final Sprite sprite;
    private final Timer shootTimer;
    private final Timer moveFistTimer;

    // TODO: remove temp move timer and bool
    private final Timer tempMoveTimer;
    private boolean tempMoveLeft;

    private Block tankBlock;
    private Block tankPlatform;
    private Block bodyBlock;
    private Sprite fistSprite;
    private Rectangle fistRect;
    private boolean moveFistDown;

    public GutsTank(MegamanGame game) {
        super(game, BodyType.ABSTRACT);

        // TODO: remove temp move timer
        tempMoveTimer = new Timer(TEMP_MOVE_DUR);

        sprite = new Sprite();
        shootTimer = new Timer(SHOOT_DUR);
        moveFistTimer = new Timer(MOVE_FIST_DUR);
        fistRect = new Rectangle().setSize(FIST_WIDTH * WorldVals.PPM, FIST_HEIGHT * WorldVals.PPM);

        // tank block
        tankBlock = new Block(game, false, WIDTH * WorldVals.PPM, 1.85f * WorldVals.PPM);

        // tank platform
        tankPlatform = new Block(game, false, WIDTH * WorldVals.PPM, .15f * WorldVals.PPM);

        // body block
        bodyBlock = new Block(game, false, 6.5f * WorldVals.PPM, (HEIGHT - 2f) * WorldVals.PPM);
        Fixture bodyShield = new Fixture(bodyBlock, FixtureType.SHIELD,
                new Rectangle().setSize(WorldVals.PPM, HEIGHT * WorldVals.PPM));
        bodyBlock.body.add(bodyShield);

        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
        runOnDeath.add(() -> {
            tankPlatform.dead = true;
            bodyBlock.dead = true;
        });
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        onSpawn();
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
        shootTimer.setToEnd();
        moveFistTimer.setToEnd();
        GameEngine e = game.getGameEngine();

        // tank block
        float tankBlockX = body.getX() + 1.5f * WorldVals.PPM;
        float tankBlockY = body.getY();
        e.spawn(tankBlock, new Vector2(tankBlockX, tankBlockY), new ObjectMap<>() {{
            put(BodyLabel.BODY_LABEL, BodyLabel.NO_SIDE_TOUCHIE);
        }});

        // tank platform
        float tankPlatformX = body.getX() + .1f * WorldVals.PPM;
        float tankPlatformY = body.getY() + 1.85f * WorldVals.PPM;
        e.spawn(tankPlatform, new Vector2(tankPlatformX, tankPlatformY), new ObjectMap<>() {{
            put(BodyLabel.BODY_LABEL, BodyLabel.COLLIDE_DOWN_ONLY + "," + BodyLabel.NO_SIDE_TOUCHIE);
        }});

        // body block
        float bodyBlockX = body.getMaxX() - 6.5f * WorldVals.PPM;
        float bodyBlockY = body.getY();
        e.spawn(bodyBlock, new Vector2(bodyBlockX, bodyBlockY), new ObjectMap<>() {{
            put(BodyLabel.BODY_LABEL, BodyLabel.NO_SIDE_TOUCHIE);
        }});

        // TODO: remove temp move timer and bool
        tempMoveTimer.reset();
        tempMoveLeft = true;

        fistRect.x = body.getX() + 1.35f * WorldVals.PPM;
        fistRect.y = body.getY() + 3f * WorldVals.PPM;

        /*

        // tank block
        tankBlock.setX(body.getX() + (1.5f * WorldVals.PPM));
        tankBlock.setY(body.getY());

        // tank platform
        tankPlatform.setX(body.getX() + .1f * WorldVals.PPM);
        tankPlatform.setMaxY(body.getY() + 2f * WorldVals.PPM);

        // body block
        bodyBlock.setMaxX(body.getMaxX());
        bodyBlock.setY(body.getY());
         */
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDmgNegs() {
        return new HashMap<>() {{

        }};
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            // TODO: remove temp move timer and bool
            tempMoveTimer.update(delta);
            if (tempMoveTimer.isJustFinished()) {
                tempMoveTimer.reset();
                tempMoveLeft = !tempMoveLeft;
            }
            float velX = TEMP_MOVE_DIST * WorldVals.PPM;
            if (tempMoveLeft) {
                velX *= -1f;
            }
            body.velocity.x = velX;
            tankBlock.body.velocity.x = velX;
            tankPlatform.body.velocity.x = velX;
            bodyBlock.body.velocity.x = velX;

            // fist
            moveFistTimer.update(delta);

            /*
            fistRect.x = body.getX() + 1.35f * WorldVals.PPM;
            fistRect.y = body.getY() + 3f * WorldVals.PPM;

            // tank block
            tankBlock.setX(body.getX() + (1.5f * WorldVals.PPM));
            tankBlock.setY(body.getY());

            // tank platform
            tankPlatform.setX(body.getX() + .1f * WorldVals.PPM);
            tankPlatform.setMaxY(body.getY() + 2f * WorldVals.PPM);

            // body block
            bodyBlock.setMaxX(body.getMaxX());
            bodyBlock.setY(body.getY());
             */
        });
    }

    public boolean isShooting() {
        return !shootTimer.isFinished();
    }

    private void startMovingFist() {
        moveFistTimer.reset();
        if (fistRect.y == FIST_UP_Y_OFFSET) {

        }
    }

    protected void defineBody() {
        body.setSize(WIDTH * WorldVals.PPM, HEIGHT * WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // tank damager fixture
        Rectangle tankDamagerRect = new Rectangle().setSize(.85f * WorldVals.PPM);
        Fixture tankDamagerFixture = new Fixture(this, FixtureType.DAMAGER, tankDamagerRect, false);
        h.add(new ShapeHandle(tankDamagerRect, Color.ORANGE));
        body.add(tankDamagerFixture);

        // body damager fixture
        Rectangle bodyDamagerRect = new Rectangle().setSize(WorldVals.PPM, 4f * WorldVals.PPM);
        Fixture bodyDamagerFixture = new Fixture(this, FixtureType.DAMAGER, bodyDamagerRect, false);
        h.add(new ShapeHandle(bodyDamagerRect, Color.ORANGE));
        body.add(bodyDamagerFixture);

        // fist damager fixture
        Fixture fistDamagerFixture = new Fixture(this, FixtureType.DAMAGER, fistRect, false);
        h.add(new ShapeHandle(fistDamagerFixture.shape, Color.ORANGE));
        body.add(fistDamagerFixture);

        // fist fixture
        Fixture fistFixture = new Fixture(this, FixtureType.SHIELD, fistRect, false);
        h.add(new ShapeHandle(fistFixture.shape, Color.GREEN));
        body.add(fistFixture);

        // eyes damageable fixture
        Fixture eyesDamageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Rectangle());
        h.add(new ShapeHandle(eyesDamageableFixture.shape, Color.PURPLE));
        body.add(eyesDamageableFixture);

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }

        body.preProcess = delta -> {
            tankDamagerRect.setX(body.getX() + 1.15f * WorldVals.PPM);
            tankDamagerRect.setY(body.getY());
            bodyDamagerRect.setX(body.getCenter().x - 2f * WorldVals.PPM);
            bodyDamagerRect.setY(body.getY() + 2f * WorldVals.PPM);
        };
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(WIDTH * WorldVals.PPM, HEIGHT * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 2);
        h.updatable = delta -> {
            h.hidden = dmgBlink;
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
        };

        // TODO: sprites for open and closed fist?
        TextureRegion fistReg = game.getAssMan().getTextureRegion(TextureAsset.GUTS_TANK, "Fist");
        fistSprite = new Sprite(fistReg);
        fistSprite.setSize(2.15f * WorldVals.PPM, 2.15f * WorldVals.PPM);
        SpriteHandle fistH = new SpriteHandle(fistSprite, 3);
        fistH.updatable = delta -> {
            fistH.hidden = dmgBlink;
            fistH.setPosition(fistRect, Position.CENTER);
        };

        return new SpriteComponent(h, fistH);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> isShooting() ? "MouthOpen" : "MouthClosed";
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.GUTS_TANK);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("MouthClosed", new Animation(atlas.findRegion("MouthClosed"), 2, .15f));
            put("MouthOpen", new Animation(atlas.findRegion("MouthOpen"), 2, .15f));
        }});
    }

}
