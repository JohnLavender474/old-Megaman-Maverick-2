package com.megaman.game.entities.projectiles.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.enemies.Enemy;
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
import com.megaman.game.world.WorldConstVals;

public class Bullet extends Projectile {

    private static final float CLAMP = 10f;

    private final Sprite sprite = new Sprite();

    private Vector2 traj;

    public Bullet(MegamanGame game, Entity owner) {
        super(game, owner);
        defineBody();
        addComponent(spriteComponent());
        addComponent(updatableComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        // trajectory
        traj = (Vector2) data.get(ConstKeys.TRAJECTORY);
        // spawn
        Vector2 spawn = (Vector2) data.get(ConstKeys.SPAWN);
        body.bounds.setCenter(spawn);
    }

    public void disintegrate() {
        dead = true;
        game.getGameEngine().spawnEntity(
                game.getEntityFactories().fetch(EntityType.EXPLOSION, "ChargedShotExplosion"),
                ShapeUtils.getCenterPoint(body.bounds));
        getComponent(SoundComponent.class).request(SoundAsset.THUMP_SOUND);
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
        String reflectDir = (String) shieldFixture.userData.get(ConstKeys.DIR);
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
        body.velClamp.set(CLAMP, CLAMP);
        // projectile fixture
        Fixture projectileFixture = new Fixture(this, FixtureType.PROJECTILE, .2f * WorldConstVals.PPM);
        body.fixtures.add(projectileFixture);
        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, .2f * WorldConstVals.PPM);
        body.fixtures.add(damagerFixture);
    }

    private SpriteComponent spriteComponent() {
        TextureRegion t = game.getAssMan().getTextureRegion(TextureAsset.OBJECTS, "YellowBullet");
        sprite.setRegion(t);
        sprite.setSize(WorldConstVals.PPM * 1.25f, WorldConstVals.PPM * 1.25f);
        SpriteHandle handle = new SpriteHandle(sprite, 4);
        handle.runnable = () -> handle.setPos(body.bounds, Position.CENTER);
        return new SpriteComponent(handle);
    }

}
