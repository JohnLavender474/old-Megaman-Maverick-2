package com.megaman.game.entities.projectiles.impl;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
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
import com.megaman.game.entities.Damageable;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.decorations.DecorationFactory;
import com.megaman.game.entities.decorations.impl.SmokePuff;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.BodyType;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

import java.util.PriorityQueue;

public class Fireball extends Projectile {

    private static final Logger logger = new Logger(Fireball.class, MegamanGame.DEBUG);

    private static final float ROTATION = 1000f;
    private static final float CULL_DUR = 1f;
    private static final float Y_BOUNCE = 10f;
    private static final float X_VEL = 10f;

    public static final int MAX_BOUNCES = 3;

    private final Timer cullTimer;

    private Fixture headFixture;
    private Fixture feetFixture;
    private Fixture leftFixture;
    private Fixture rightFixture;

    private int bounces;
    private float xVel;

    public Fireball(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        cullTimer = new Timer(CULL_DUR);
        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
        putComponent(updatableComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        super.init(spawn, data);
        bounces = 0;
        cullTimer.reset();
        body.gravityOn = true;
        boolean left = (boolean) data.get(ConstKeys.LEFT);
        xVel = X_VEL * WorldVals.PPM;
        if (left) {
            xVel *= -1f;
        }
        body.velocity.x = xVel;
        body.velocity.y = Y_BOUNCE * WorldVals.PPM;
    }

    @Override
    public void hitBody(Fixture bodyFixture) {
        if (UtilMethods.mask(owner, bodyFixture.entity, o -> o instanceof Megaman, o -> o instanceof Enemy)) {
            bounces = MAX_BOUNCES;
            getComponent(SoundComponent.class).request(SoundAsset.ATOMIC_FIRE_SOUND);
        }
    }

    @Override
    public void hitBlock(Fixture blockFixture) {
        logger.log("Fireball hit block");
        hitBlockOrShield(blockFixture);
    }

    @Override
    public void hitShield(Fixture shieldFixture) {
        hitBlockOrShield(shieldFixture);
        getComponent(SoundComponent.class).request(SoundAsset.DINK_SOUND);
    }

    @Override
    public void hitWater(Fixture waterFixture) {
        logger.log("Fireball hit water");
        dead = true;
        SmokePuff puff = (SmokePuff) game.getEntityFactories()
                .fetch(EntityType.DECORATION, DecorationFactory.SMOKE_PUFF);
        Rectangle r = ShapeUtils.getBoundingRect(waterFixture.shape);
        Vector2 pos = new Vector2(body.getCenter().x, r.y + r.height);
        game.getGameEngine().spawnEntity(puff, pos);
        Sound steamSound = game.getAssMan().getSound(SoundAsset.WHOOSH_SOUND);
        game.getAudioMan().playSound(steamSound, false);
    }

    private void hitBlockOrShield(Fixture fixture) {
        Rectangle bounds;
        if (fixture.shape instanceof Rectangle r) {
            bounds = r;
        } else if (fixture.shape instanceof Circle c) {
            bounds = new Rectangle().setSize(c.radius * 2f).setCenter(c.x, c.y);
        } else {
            return;
        }
        Fixture f = getFixtureWithMostOverlap(bounds);
        if (f.equals(leftFixture)) {
            xVel = X_VEL * WorldVals.PPM;
        } else if (f.equals(rightFixture)) {
            xVel = -X_VEL * WorldVals.PPM;
        } else if (f.equals(headFixture)) {
            body.velocity.y = -Y_BOUNCE * WorldVals.PPM;
        } else {
            body.velocity.y = Y_BOUNCE * WorldVals.PPM;
        }
        bounces++;
        if (bounces == MAX_BOUNCES) {
            getComponent(SoundComponent.class).request(SoundAsset.ATOMIC_FIRE_SOUND);
        }
    }

    @Override
    public void onDamageInflictedTo(Damageable damageable) {
        bounces = MAX_BOUNCES;
    }

    private Fixture getFixtureWithMostOverlap(Rectangle bounds) {
        PriorityQueue<Fixture> p = new PriorityQueue<>((f1, f2) -> {
            Rectangle o1 = new Rectangle();
            Intersector.intersectRectangles((Rectangle) f1.shape, bounds, o1);
            Rectangle o2 = new Rectangle();
            Intersector.intersectRectangles((Rectangle) f2.shape, bounds, o2);
            return Float.compare(o2.area(), o1.area());
        });
        p.add(leftFixture);
        p.add(rightFixture);
        p.add(headFixture);
        p.add(feetFixture);
        return p.poll();
    }

    private void defineBody() {
        ShapeComponent s = new ShapeComponent();
        putComponent(s);
        body.gravity.y = -.5f * WorldVals.PPM;
        body.bounds.setSize(.9f * WorldVals.PPM, .9f * WorldVals.PPM);
        Fixture headFixture = new Fixture(this, FixtureType.HEAD,
                new Rectangle().setSize(WorldVals.PPM / 2f, WorldVals.PPM / 32f));
        headFixture.offset.y = WorldVals.PPM / 2f;
        body.fixtures.add(headFixture);
        this.headFixture = headFixture;
        s.shapeHandles.add(new ShapeHandle(headFixture.shape, Color.BLUE));
        Fixture feetFixture = new Fixture(this, FixtureType.FEET,
                new Rectangle().setSize(WorldVals.PPM / 2f, WorldVals.PPM / 32f));
        feetFixture.offset.y = -WorldVals.PPM / 2f;
        body.fixtures.add(feetFixture);
        this.feetFixture = feetFixture;
        s.shapeHandles.add(new ShapeHandle(feetFixture.shape, Color.BLUE));
        Fixture leftFixture = new Fixture(this, FixtureType.SIDE,
                new Rectangle().setSize(WorldVals.PPM / 32f, WorldVals.PPM));
        leftFixture.offset.x = -WorldVals.PPM / 2f;
        leftFixture.putUserData(ConstKeys.SIDE, ConstKeys.LEFT);
        body.fixtures.add(leftFixture);
        this.leftFixture = leftFixture;
        s.shapeHandles.add(new ShapeHandle(leftFixture.shape, Color.BLUE));
        Fixture rightFixture = new Fixture(this, FixtureType.SIDE,
                new Rectangle().setSize(WorldVals.PPM / 32f, WorldVals.PPM));
        rightFixture.offset.x = WorldVals.PPM / 2f;
        rightFixture.putUserData(ConstKeys.SIDE, ConstKeys.RIGHT);
        body.fixtures.add(rightFixture);
        this.rightFixture = rightFixture;
        s.shapeHandles.add(new ShapeHandle(rightFixture.shape, Color.BLUE));
        Fixture projFixture = new Fixture(this, FixtureType.PROJECTILE,
                new Rectangle().setSize(.9f * WorldVals.PPM));
        body.fixtures.add(projFixture);
        Fixture dmgrFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.9f * WorldVals.PPM));
        body.fixtures.add(dmgrFixture);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            if (bounces == MAX_BOUNCES) {
                body.gravityOn = false;
                body.velocity.setZero();
                cullTimer.update(delta);
            } else {
                body.velocity.x = xVel;
            }
            if (cullTimer.isFinished()) {
                dead = true;
            }
        });
    }

    private AnimationComponent animationComponent() {
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.FIRE);
        return new AnimationComponent(sprite, () -> bounces == MAX_BOUNCES ? "Flame" : "Fireball", new ObjectMap<>() {{
            put("Flame", new Animation(atlas.findRegion("Flame"), 4, .1f));
            put("Fireball", new Animation(atlas.findRegion("Fireball")));
        }});
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.25f * WorldVals.PPM, 1.25f * WorldVals.PPM);
        sprite.setOrigin(sprite.getWidth() / 2f, sprite.getHeight() / 2f);
        SpriteHandle h = new SpriteHandle(sprite, 3);
        h.updatable = delta -> {
            if (bounces == MAX_BOUNCES) {
                sprite.setRotation(0f);
            } else {
                sprite.rotate(ROTATION * delta);
            }
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
        };
        return new SpriteComponent(h);
    }

}
