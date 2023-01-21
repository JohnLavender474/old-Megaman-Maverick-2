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
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.utils.damage.DamageNegotiation;
import com.megaman.game.entities.utils.damage.Damager;
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

public class GutsTank extends Enemy {

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
    private static final float MOVE_FIST_VEL_DIST = 1f;

    private final Sprite sprite;

    private final Timer shootTimer;
    private final Timer moveFistTimer;

    private Block tankBlock;
    private Block bodyBlock;
    private Sprite fistSprite;
    private Rectangle fistRect;

    private boolean moveFistUp;

    public GutsTank(MegamanGame game) {
        super(game, BodyType.ABSTRACT);
        sprite = new Sprite();
        shootTimer = new Timer(SHOOT_DUR);
        moveFistTimer = new Timer(MOVE_FIST_DUR);
        fistRect = new Rectangle().setSize(FIST_WIDTH * WorldVals.PPM, FIST_HEIGHT * WorldVals.PPM);
        tankBlock = new Block(game, false, WIDTH * WorldVals.PPM, WorldVals.PPM / 8f);
        tankBlock.addBodyLabels(BodyLabel.COLLIDE_DOWN_ONLY, BodyLabel.NO_SIDE_TOUCHIE);
        bodyBlock = new Block(game, false, WorldVals.PPM / 8f, HEIGHT * WorldVals.PPM);
        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
        runOnDeath.add(() -> {
            tankBlock.dead = true;
            bodyBlock.dead = true;
        });
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
        shootTimer.setToEnd();
        moveFistTimer.setToEnd();
        moveFistUp = false;
        GameEngine e = game.getGameEngine();
        e.spawn(tankBlock, new Vector2());
        e.spawn(bodyBlock, new Vector2());
    }

    public boolean isShooting() {
        return !shootTimer.isFinished();
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {

        });
    }

    protected void defineBody() {
        body.setSize(WIDTH * WorldVals.PPM, HEIGHT * WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // tank block fixture
        Fixture tankBlockFixture = new Fixture(this, FixtureType.CUSTOM, tankBlock.body);
        // TODO: set y offset
        tankBlockFixture.offset.y = 0f;
        body.add(tankBlockFixture);

        // body block fixture
        Fixture bodyBlockFixture = new Fixture(this, FixtureType.CUSTOM, bodyBlock.body);
        // TODO: set x offset
        bodyBlockFixture.offset.x = 0f;
        body.add(bodyBlockFixture);

        // body shield fixture
        // TODO: set body shield size
        Fixture bodyShieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(0f, 0f));
        // TODO: set x offset
        bodyShieldFixture.offset.x = 0f;
        h.add(new ShapeHandle(bodyShieldFixture.shape, Color.GREEN));
        body.add(bodyShieldFixture);

        // tank shield fixture
        // TODO: set tank shield size
        Fixture tankShieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(0f, 0f));
        // TODO: set y offset
        tankShieldFixture.offset.y = 0f;
        h.add(new ShapeHandle(tankShieldFixture.shape, Color.GREEN));
        body.add(tankShieldFixture);

        // hand fixture
        Fixture handFixture = new Fixture(this, FixtureType.SHIELD, fistRect, false);
        h.add(new ShapeHandle(handFixture.shape, Color.GREEN));
        body.add(handFixture);

        // hand damager fixture
        Fixture handDamagerFixture = new Fixture(this, FixtureType.DAMAGER, fistRect, false);
        h.add(new ShapeHandle(handDamagerFixture.shape, Color.RED));
        body.add(handDamagerFixture);

        // eyes damageable fixture
        Fixture eyesDamageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Rectangle());
        h.add(new ShapeHandle(eyesDamageableFixture.shape, Color.PURPLE));
        body.add(eyesDamageableFixture);

    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(WIDTH * WorldVals.PPM, HEIGHT * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 5);
        h.updatable = delta -> {
            h.hidden = dmgBlink;
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
        };

        // TODO: sprites for open and closed fist?
        TextureRegion fistReg = game.getAssMan().getTextureRegion(TextureAsset.GUTS_TANK, "Fist");
        fistSprite = new Sprite(fistReg);
        fistSprite.setSize(1.25f * WorldVals.PPM, 1.25f * WorldVals.PPM);
        SpriteHandle fistH = new SpriteHandle(fistSprite, 6);
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
