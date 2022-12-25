package com.megaman.game.entities.projectiles.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.entities.Damageable;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.explosions.ExplosionFactory;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.BodyType;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

public class Bullet extends Projectile {

    private static final float CLAMP = 10f;
    private static final float CULL_DUR = .2f;
    private static final float REFLECT_VEL = 5f;

    private static TextureRegion bulletReg;

    private final Vector2 traj;

    public Bullet(MegamanGame game) {
        super(game, CULL_DUR, BodyType.ABSTRACT);
        if (bulletReg == null) {
            bulletReg = game.getAssMan().getTextureRegion(TextureAsset.OBJECTS, "YellowBullet");
        }
        this.traj = new Vector2();
        defineBody();
        putComponent(spriteComponent());

        // TODO: testing NOT using updatable comp
        // putComponent(updatableComponent());
    }

    @Override
    public void onDamageInflictedTo(Damageable damageable) {
        disintegrate();
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        traj.set((Vector2) data.get(ConstKeys.TRAJECTORY)).scl(WorldVals.PPM);

        // TODO: testing NOT using updatable comp
        body.velocity.set(traj);

        super.init(spawn, data);
    }

    public void disintegrate() {
        dead = true;
        game.getGameEngine().spawnEntity(
                game.getEntityFactories().fetch(EntityType.EXPLOSION, ExplosionFactory.DISINTEGRATION),
                ShapeUtils.getCenterPoint(body.bounds));
        game.getAudioMan().playSound(SoundAsset.THUMP_SOUND);
    }

    @Override
    public void hitBlock(Fixture blockFixture) {
        disintegrate();
    }

    @Override
    public void hitBody(Fixture body) {
        if (UtilMethods.mask(owner, body.entity, o -> o instanceof Megaman, o -> o instanceof Enemy)) {
            disintegrate();
        }
    }

    @Override
    public void hitShield(Fixture shieldFixture) {
        owner = shieldFixture.entity;
        traj.x *= -1f;
        String reflectDir = shieldFixture.getUserData(ConstKeys.REFLECT, String.class);
        if (reflectDir == null || reflectDir.equals(ConstKeys.STRAIGHT)) {
            traj.y = 0f;
        } else if (reflectDir.equals(ConstKeys.UP)) {
            traj.y = REFLECT_VEL * WorldVals.PPM;
        } else {
            traj.y = -REFLECT_VEL * WorldVals.PPM;
        }

        // TODO: testing NOT using updatable comp
        body.velocity.set(traj);

        getComponent(SoundComponent.class).requestToPlay(SoundAsset.DINK_SOUND);
    }

    // TODO: testing NOT using updatable comp
    /*
    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> body.velocity.set(traj));
    }
     */

    private void defineBody() {
        body.velClamp.set(CLAMP * WorldVals.PPM, CLAMP * WorldVals.PPM);
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY, new Rectangle(body.bounds));
        body.fixtures.add(bodyFixture);
        Fixture projectileFixture = new Fixture(this, FixtureType.PROJECTILE,
                new Rectangle().setSize(.2f * WorldVals.PPM, .2f * WorldVals.PPM));
        body.fixtures.add(projectileFixture);
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.2f * WorldVals.PPM, .2f * WorldVals.PPM));
        body.fixtures.add(damagerFixture);
    }

    private SpriteComponent spriteComponent() {
        sprite.setRegion(bulletReg);
        sprite.setSize(WorldVals.PPM * 1.25f, WorldVals.PPM * 1.25f);
        SpriteHandle h = new SpriteHandle(sprite, 5);
        h.updatable = delta -> h.setPosition(body.bounds, Position.CENTER);
        return new SpriteComponent(h);
    }

}
