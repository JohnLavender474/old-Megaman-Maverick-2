package com.megaman.game.entities.projectiles.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.explosions.ExplosionFactory;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

public class Bullet extends Projectile {

    private static final float CLAMP = 10f;

    private static TextureRegion bulletReg;

    private final Vector2 traj;

    public Bullet(MegamanGame game) {
        super(game);
        if (bulletReg == null) {
            bulletReg = game.getAssMan().getTextureRegion(TextureAsset.OBJECTS, "YellowBullet");
        }
        this.traj = new Vector2();
        defineBody();
        putComponent(spriteComponent());
        putComponent(updatableComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        traj.set((Vector2) data.get(ConstKeys.TRAJECTORY)).scl(WorldVals.PPM);
        super.init(spawn, data);
    }

    public void disintegrate() {
        dead = true;
        game.getGameEngine().spawnEntity(
                game.getEntityFactories().fetch(EntityType.EXPLOSION, ExplosionFactory.DISINTEGRATION),
                ShapeUtils.getCenterPoint(body.bounds));
        game.getAudioMan().playSound(game.getAssMan().getSound(SoundAsset.THUMP_SOUND), false);
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
        String reflectDir = shieldFixture.getUserData(ConstKeys.DIR, String.class);
        if (reflectDir == null || reflectDir.equals(ConstKeys.STRAIGHT)) {
            traj.y = 0f;
        } else {
            traj.y = 5f;
            if (reflectDir.equals(ConstKeys.DOWN)) {
                traj.y *= -1f;
            }
        }
        getComponent(SoundComponent.class).request(SoundAsset.DINK_SOUND);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> body.velocity.set(traj));
    }

    private void defineBody() {
        body.velClamp.set(CLAMP * WorldVals.PPM, CLAMP * WorldVals.PPM);
        // projectile fixture
        Fixture projectileFixture = new Fixture(this, FixtureType.PROJECTILE, .2f * WorldVals.PPM);
        body.fixtures.add(projectileFixture);
        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, .2f * WorldVals.PPM);
        body.fixtures.add(damagerFixture);
    }

    private SpriteComponent spriteComponent() {
        sprite.setRegion(bulletReg);
        sprite.setSize(WorldVals.PPM * 1.25f, WorldVals.PPM * 1.25f);
        SpriteHandle h = new SpriteHandle(sprite, 3);
        h.updatable = delta -> h.setPosition(body.bounds, Position.CENTER);
        return new SpriteComponent(h);
    }

}
