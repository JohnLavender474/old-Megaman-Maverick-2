package com.megaman.game.entities.impl.projectiles.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.utils.damage.Damageable;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.impl.explosions.ExplosionFactory;
import com.megaman.game.entities.impl.megaman.Megaman;
import com.megaman.game.entities.impl.projectiles.Projectile;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

public class Snowball extends Projectile {

    private static final float CLAMP = 10f;

    private static TextureRegion snowBallReg;

    public Snowball(MegamanGame game) {
        super(game);
        if (snowBallReg == null) {
            snowBallReg = game.getAssMan().getTextureRegion(TextureAsset.PROJECTILES_1, "Snowball");
        }
        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    @Override
    public void onDamageInflictedTo(Damageable damageable) {
        explode();
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        super.init(spawn, data);
        if (data.containsKey(ConstKeys.TRAJECTORY)) {
            body.velocity.set((Vector2) data.get(ConstKeys.TRAJECTORY));
        } else {
            body.velocity.setZero();
        }
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
        dead = true;
        Entity e = game.getEntityFactories().fetch(EntityType.EXPLOSION, ExplosionFactory.SNOWBALL_EXPLOSION);
        game.getGameEngine().spawn(e, body.getCenter(), new ObjectMap<>() {{
            put(ConstKeys.MASK, new ObjectSet<>() {{
                add(owner == game.getMegaman() ? Enemy.class : Megaman.class);
            }});
        }});
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

    private SpriteComponent spriteComponent() {
        sprite.setSize(.85f * WorldVals.PPM, .85f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 5);
        h.updatable = delta -> h.setPosition(body.bounds, Position.CENTER);
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(snowBallReg));
    }

}
