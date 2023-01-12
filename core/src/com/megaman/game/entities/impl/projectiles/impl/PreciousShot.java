package com.megaman.game.entities.impl.projectiles.impl;

import com.badlogic.gdx.graphics.Color;
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
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.impl.explosions.ExplosionFactory;
import com.megaman.game.entities.impl.explosions.impl.PreciousExplosion;
import com.megaman.game.entities.impl.projectiles.Projectile;
import com.megaman.game.entities.impl.projectiles.ProjectileFactory;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

import java.util.function.Supplier;

public class PreciousShot extends Projectile {

    private static final float FORMING_DUR = 1f;
    private static final float PRECIOUS_X = 8f;
    private static final float PRECIOUS_Y = 4f;

    private final Timer formingTimer;
    private final Vector2 traj;

    private boolean small;
    private boolean left;

    public PreciousShot(MegamanGame game) {
        super(game);
        formingTimer = new Timer(FORMING_DUR);
        traj = new Vector2();
        small = false;
        defineBody();
        putComponent(updatableComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        small = data.containsKey(ConstKeys.BOOL) && (boolean) data.get(ConstKeys.BOOL);
        body.setSize((small ? .15f : .75f) * WorldVals.PPM);
        if (small) {
            formingTimer.setToEnd();
        } else {
            formingTimer.reset();
        }
        left = (boolean) data.get(ConstKeys.LEFT);
        traj.x = (left ? -PRECIOUS_X : PRECIOUS_X) * WorldVals.PPM;
        if (data.containsKey(ConstKeys.DIR)) {
            String dir = (String) data.get(ConstKeys.DIR);
            float yVel;
            if (dir.equals(ConstKeys.UP)) {
                yVel = PRECIOUS_Y * WorldVals.PPM;
            } else if (dir.equals(ConstKeys.STRAIGHT)) {
                yVel = 0f;
            } else {
                yVel = -PRECIOUS_Y * WorldVals.PPM;
            }
            traj.y = yVel;
        } else {
            traj.y = 0f;
        }
        sprite.setSize((small ? .5f : 1.65f) * WorldVals.PPM, (small ? .5f : 1.65f) * WorldVals.PPM);
        sprite.setOrigin(sprite.getWidth() / 2f, sprite.getHeight() / 2f);
        super.init(spawn, data);
    }

    @Override
    public void hitBody(Fixture bodyFixture) {
        // TODO: some bodies can be "frozen" in crystal
        /*
        if (bodyFixture.entity instanceof Example e) {
            e.setFrozenInCrystal(true);
        }
        */
    }

    @Override
    public void hitBlock(Fixture blockFixture) {
        shatter();
    }

    @Override
    public void hitShield(Fixture shieldFixture) {
        if (shieldFixture.entity instanceof Projectile) {
            return;
        }
        shatter();
        // TODO: break shield if (...)?
    }

    public boolean isForming() {
        return !formingTimer.isFinished();
    }

    private void shatter() {
        dead = true;
        PreciousExplosion e = (PreciousExplosion) game.getEntityFactories().fetch(EntityType.EXPLOSION,
                ExplosionFactory.PRECIOUS_EXPLOSION);
        game.getGameEngine().spawn(e, body.getCenter());
        if (small) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            PreciousShot s = (PreciousShot) game.getEntityFactories().fetch(EntityType.PROJECTILE,
                    ProjectileFactory.PRECIOUS_SHOT);
            String dir = switch (i) {
                case 0 -> ConstKeys.UP;
                case 1 -> ConstKeys.STRAIGHT;
                case 2 -> ConstKeys.DOWN;
                default -> throw new IllegalStateException("Illegal state");
            };
            game.getGameEngine().spawn(s, body.getCenter(), new ObjectMap<>() {{
                put(ConstKeys.OWNER, owner);
                put(ConstKeys.LEFT, !left);
                put(ConstKeys.BOOL, true);
                put(ConstKeys.DIR, dir);
            }});
        }
    }

    private void defineBody() {
        Array<ShapeHandle> h = new Array<>();

        // projectile fixture
        Fixture projectileFixture = new Fixture(this, FixtureType.PROJECTILE, new Rectangle());
        h.add(new ShapeHandle(projectileFixture.shape, Color.BLUE));
        body.add(projectileFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Rectangle());
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);

        // shield fixture
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD, new Rectangle());
        shieldFixture.putUserData(ConstKeys.REFLECT, ConstKeys.STRAIGHT);
        h.add(new ShapeHandle(shieldFixture.shape, Color.GREEN));
        body.add(shieldFixture);

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }

        // pre-process
        body.preProcess = delta -> {
            Vector2 size = new Vector2();
            size.y = (small ? .15f : .5f) * WorldVals.PPM;
            if (isForming()) {
                body.velocity.setZero();
                size.x = .5f * WorldVals.PPM;
            } else {
                body.velocity.set(traj);
                size.x = (small ? .15f : .75f) * WorldVals.PPM;
            }
            ((Rectangle) projectileFixture.shape).setSize(size.x, size.y);
            ((Rectangle) damagerFixture.shape).setSize(size.x, size.y);
            ((Rectangle) shieldFixture.shape).setSize(size.x, size.y);
        };
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(formingTimer::update);
    }

    private SpriteComponent spriteComponent() {
        SpriteHandle h = new SpriteHandle(sprite, 5);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.CENTER);
            sprite.setFlip(left, false);
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> isForming() ? "Form" : "Shoot";
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.PROJECTILES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Form", new Animation(atlas.findRegion("PreciousShot/Form"), 9, .1f, false));
            put("Shoot", new Animation(atlas.findRegion("PreciousShot/Shoot"), 3, .1f));
        }});
    }

}
