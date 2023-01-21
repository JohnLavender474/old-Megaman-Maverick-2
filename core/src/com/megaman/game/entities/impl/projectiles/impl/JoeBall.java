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
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.entities.*;
import com.megaman.game.entities.utils.damage.Damageable;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.impl.explosions.ExplosionFactory;
import com.megaman.game.entities.impl.megaman.Megaman;
import com.megaman.game.entities.impl.projectiles.Projectile;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.BodyType;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

public class JoeBall extends Projectile {

    public static final String SNOW_TYPE = "Snow";

    private static final float CLAMP = 15f;
    private static final float CULL_DUR = .2f;
    private static final float REFLECT_VEL = 5f;

    private static TextureRegion joeBallReg;
    private static TextureRegion snowJoeBallReg;

    private final Vector2 traj;

    private String type;

    public JoeBall(MegamanGame game) {
        super(game, CULL_DUR, BodyType.ABSTRACT);
        if (joeBallReg == null) {
            joeBallReg = game.getAssMan().getTextureRegion(TextureAsset.PROJECTILES_1, "Joeball");
        }
        if (snowJoeBallReg == null) {
            snowJoeBallReg = game.getAssMan().getTextureRegion(TextureAsset.PROJECTILES_1, "SnowJoeball");
        }
        type = "";
        traj = new Vector2();
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
        traj.set((Vector2) data.get(ConstKeys.TRAJECTORY)).scl(WorldVals.PPM);
        body.velocity.set(traj);
        type = data.containsKey(ConstKeys.TYPE) ? (String) data.get(ConstKeys.TYPE) : "";
        super.init(spawn, data);
    }

    @Override
    public void hitBlock(Fixture blockFixture) {
        explode();
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
        body.velocity.set(traj);
        getComponent(SoundComponent.class).requestToPlay(SoundAsset.DINK_SOUND);
    }

    public void explode() {
        dead = true;
        String explosionType;
        SoundAsset sound;
        switch (type) {
            case SNOW_TYPE -> {
                sound = SoundAsset.THUMP_SOUND;
                explosionType = ExplosionFactory.SNOWBALL_EXPLOSION;
            }
            default -> {
                sound = SoundAsset.EXPLOSION_SOUND;
                explosionType = ExplosionFactory.EXPLOSION;
            }
        }
        Entity explosion = game.getEntityFactories().fetch(EntityType.EXPLOSION, explosionType);
        game.getGameEngine().spawn(explosion, body.getCenter(), new ObjectMap<>() {{
            put(ConstKeys.MASK, new ObjectSet<>() {{
                add(owner == game.getMegaman() ? Enemy.class : Megaman.class);
            }});
        }});
        game.getAudioMan().play(sound);
    }

    private void defineBody() {
        body.bounds.setSize(.15f * WorldVals.PPM);
        body.velClamp.set(CLAMP * WorldVals.PPM, CLAMP * WorldVals.PPM);

        // body fixture
        Fixture bodyFixture = new Fixture(this, FixtureType.BODY, new Rectangle(body.bounds));
        body.add(bodyFixture);

        // projectile fixture
        Fixture projectileFixture = new Fixture(this, FixtureType.PROJECTILE,
                new Rectangle().setSize(.2f * WorldVals.PPM, .2f * WorldVals.PPM));
        body.add(projectileFixture);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.2f * WorldVals.PPM, .2f * WorldVals.PPM));
        body.add(damagerFixture);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.25f * WorldVals.PPM, 1.25f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            sprite.setFlip(traj.x < 0f, false);
            h.setPosition(body.bounds, Position.CENTER);
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, () -> type, new ObjectMap<>() {{
            put("", new Animation(joeBallReg, 4, .1f));
            put(SNOW_TYPE, new Animation(snowJoeBallReg, 4, .1f));
        }});
    }

}
