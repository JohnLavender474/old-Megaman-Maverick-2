package com.megaman.game.entities.impl.enemies.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.*;
import com.megaman.game.entities.utils.damage.DamageNegotiation;
import com.megaman.game.entities.utils.damage.Damager;
import com.megaman.game.entities.utils.faceable.Faceable;
import com.megaman.game.entities.utils.faceable.Facing;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.impl.megaman.Megaman;
import com.megaman.game.entities.impl.projectiles.ProjectileFactory;
import com.megaman.game.entities.impl.projectiles.impl.JoeBall;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SwinginJoe extends Enemy implements Faceable {

    private static final float BALL_SPEED = 9f;
    private static final float SETTING_DUR = .8f;

    private static final int MAX_SETTING = 2;
    private static final int SWING_EYES_CLOSED = 0;
    private static final int SWING_EYES_OPEN = 1;
    private static final int THROWING = 2;

    private final Timer settingTimer;
    private final Sprite sprite;

    @Getter
    @Setter
    private Facing facing;
    private String type;
    private int setting;

    public SwinginJoe(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        type = "";
        sprite = new Sprite();
        settingTimer = new Timer(SETTING_DUR);
        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    public void shoot() {
        Vector2 spawn = new Vector2().set(body.getCenter()).add(
                (is(Facing.LEFT) ? -.2f : .2f) * WorldVals.PPM, .15f * WorldVals.PPM);
        ObjectMap<String, Object> data = new ObjectMap<>();
        data.put(ConstKeys.OWNER, this);
        data.put(ConstKeys.TRAJECTORY, new Vector2(
                (is(Facing.LEFT) ? -BALL_SPEED : BALL_SPEED) * WorldVals.PPM, 0f));
        data.put(ConstKeys.MASK, new ObjectSet<>() {{
            add(Megaman.class);
        }});
        data.put(ConstKeys.TYPE, type);
        JoeBall joeBall = (JoeBall) game.getEntityFactories().fetch(
                EntityType.PROJECTILE, ProjectileFactory.JOEBALL);
        game.getGameEngine().spawn(joeBall, spawn, data);
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        settingTimer.reset();
        setting = SWING_EYES_CLOSED;
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
        type = data.containsKey(ConstKeys.TYPE) ? (String) data.get(ConstKeys.TYPE) : "";
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    protected void defineBody() {
        body.bounds.setSize(WorldVals.PPM, 1.25f * WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.75f * WorldVals.PPM, 1.15f * WorldVals.PPM));
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(.8f * WorldVals.PPM, 1.35f * WorldVals.PPM));
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);

        // shield fixture
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(.4f * WorldVals.PPM, .9f * WorldVals.PPM));
        shieldFixture.putUserData(ConstKeys.REFLECT, ConstKeys.STRAIGHT);
        h.add(new ShapeHandle(shieldFixture.shape, () -> setting == SWING_EYES_CLOSED ? Color.GREEN : Color.GRAY));
        body.add(shieldFixture);

        // pre-process
        body.preProcess = delta -> {
            shieldFixture.active = setting == SWING_EYES_CLOSED;
            if (setting == 0) {
                damageableFixture.offset.x = (is(Facing.LEFT) ? .25f : -.25f) * WorldVals.PPM;
                shieldFixture.offset.x = (is(Facing.LEFT) ? -.35f : .35f) * WorldVals.PPM;
            } else {
                damageableFixture.offset.setZero();
            }
        };

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            setFacing(game.getMegaman().body.isRightOf(body) ? Facing.RIGHT : Facing.LEFT);
            settingTimer.update(delta);
            if (settingTimer.isJustFinished()) {
                setting++;
                if (setting == THROWING) {
                    shoot();
                } else if (setting > MAX_SETTING) {
                    setting = 0;
                }
                settingTimer.reset();
            }
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(2.25f * WorldVals.PPM, 2.25f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
            if (is(Facing.RIGHT)) {
                sprite.translateX(-.515f * WorldVals.PPM);
            }
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> type + switch (setting) {
            case SWING_EYES_CLOSED -> "SwingBall1";
            case SWING_EYES_OPEN -> "SwingBall2";
            case THROWING -> "ThrowBall";
            default -> throw new IllegalStateException("Setting must be between 0 and 2 inclusive");
        };
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("SwingBall1", new Animation(atlas.findRegion("SwinginJoe/SwingBall1"), 4, .1f));
            put("SwingBall2", new Animation(atlas.findRegion("SwinginJoe/SwingBall2"), 4, .1f));
            put("ThrowBall", new Animation(atlas.findRegion("SwinginJoe/ThrowBall")));
            put("SnowSwingBall1", new Animation(atlas.findRegion("SwinginJoe/SnowSwingBall1"), 4, .1f));
            put("SnowSwingBall2", new Animation(atlas.findRegion("SwinginJoe/SnowSwingBall2"), 4, .1f));
            put("SnowThrowBall", new Animation(atlas.findRegion("SwinginJoe/SnowThrowBall")));
        }});
    }

}
