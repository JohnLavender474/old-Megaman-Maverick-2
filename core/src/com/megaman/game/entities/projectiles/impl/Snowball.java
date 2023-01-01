package com.megaman.game.entities.projectiles.impl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.Damageable;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

import java.util.function.Supplier;

public class Snowball extends Projectile {

    private static final float CLAMP = 10f;
    private static final float EXPLODE_DUR = .15f;

    public final Vector2 traj;

    private final Timer explodeTimer;

    private boolean exploding;

    public Snowball(MegamanGame game) {
        super(game);
        traj = new Vector2();
        explodeTimer = new Timer(EXPLODE_DUR);
        defineBody();
        putComponent(updatableComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void onDamageInflictedTo(Damageable damageable) {
        explode();
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        if (data.containsKey(ConstKeys.TRAJECTORY)) {
            body.velocity.set((Vector2) data.get(ConstKeys.TRAJECTORY));
        } else {
            body.velocity.setZero();
        }
        exploding = false;
        explodeTimer.reset();
        super.init(spawn, data);
    }

    @Override
    public void hitBlock(Fixture blockFixture) {
        explode();
    }

    @Override
    public void hitShield(Fixture shieldFixture) {
        explode();
    }

    @Override
    public void hitWater(Fixture waterFixture) {
        explode();
    }

    @Override
    public void hitBody(Fixture bodyFixture) {
        explode();
    }

    public void explode() {
        exploding = true;
        explodeTimer.reset();
    }

    private void defineBody() {
        body.bounds.setSize(.15f * WorldVals.PPM);
        body.velClamp.set(CLAMP * WorldVals.PPM, CLAMP * WorldVals.PPM);

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY, new Rectangle(body.bounds));
        body.add(bodyFixture);

        // projectile fixture
        Fixture projectileFixture = new Fixture(this, FixtureType.PROJECTILE,
                new Rectangle().setSize(.2f * WorldVals.PPM));
        body.add(projectileFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.2f * WorldVals.PPM));
        body.add(damagerFixture);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            if (exploding) {
                explodeTimer.update(delta);
                if (explodeTimer.isFinished()) {
                    dead = true;
                }
            }
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(.85f * WorldVals.PPM, .85f * WorldVals.PPM);
        sprite.setOrigin(sprite.getWidth() / 2f, sprite.getHeight() / 2f);
        SpriteHandle h = new SpriteHandle(sprite, 5);
        h.updatable = delta -> h.setPosition(body.bounds, Position.CENTER);
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> exploding ? "SnowballExplode" : "Snowball";
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.SNOW);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Snowball", new Animation(atlas.findRegion("Snowball")));
            put("SnowballExplode", new Animation(atlas.findRegion("SnowballExplode"), 3, .05f, false));
        }});
    }

}
