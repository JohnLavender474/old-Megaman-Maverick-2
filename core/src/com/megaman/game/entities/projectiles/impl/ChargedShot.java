package com.megaman.game.entities.projectiles.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.animations.Animator;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.entities.*;
import com.megaman.game.entities.explosions.ExplosionFactory;
import com.megaman.game.entities.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;
import lombok.Getter;
import lombok.Setter;

import static com.megaman.game.assets.TextureAsset.MEGAMAN_CHARGED_SHOT;
import static com.megaman.game.assets.TextureAsset.MEGAMAN_HALF_CHARGED_SHOT;

public class ChargedShot extends Projectile implements Faceable {

    private final Sprite sprite;
    private final Vector2 traj;

    private boolean fullyCharged;
    @Getter
    @Setter
    private Facing facing;

    public ChargedShot(MegamanGame game) {
        super(game);
        this.sprite = new Sprite();
        this.traj = new Vector2();
        defineBody();
        putComponent(shapeComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());
        putComponent(updatableComponent());
    }

    @Override
    public void init(Vector2 center, ObjectMap<String, Object> data) {
        super.init(center, data);
        // fully charged?
        fullyCharged = (boolean) data.get(ConstKeys.BOOL);
        // bounds of sprite, body, and fixtures
        float bodyDim = WorldVals.PPM;
        float spriteDim = WorldVals.PPM;
        if (fullyCharged) {
            spriteDim *= 1.75f;
        } else {
            bodyDim /= 2f;
            spriteDim *= 1.25f;
        }
        sprite.setSize(spriteDim, spriteDim);
        body.bounds.setSize(bodyDim, bodyDim);
        body.bounds.setCenter(center);
        for (Fixture f : body.fixtures) {
            f.bounds.set(body.bounds);
        }
        // trajectory
        traj.set((Vector2) data.get(ConstKeys.TRAJECTORY)).scl(WorldVals.PPM);
        // facing
        facing = traj.x > 0f ? Facing.RIGHT : Facing.LEFT;
    }

    @Override
    public void hitBody(Fixture bodyFixture) {
        if (bodyFixture.entity.equals(owner)) {
            return;
        }
        dead = true;
        ChargedShotExplosion e = (ChargedShotExplosion) game.getEntityFactories()
                .fetch(EntityType.EXPLOSION, ExplosionFactory.CHARGED_SHOT_EXPLOSION);
        ObjectMap<String, Object> data = new ObjectMap<>() {{
            put(ConstKeys.OWNER, owner);
            put(ConstKeys.DIR, facing);
            put(ConstKeys.BOOL, fullyCharged);
        }};
        game.getGameEngine().spawnEntity(e, ShapeUtils.getCenterPoint(body.bounds), data);
    }

    @Override
    public void hitBlock(Fixture blockFixture) {
        dead = true;
        ChargedShotExplosion e = (ChargedShotExplosion) game.getEntityFactories()
                .fetch(EntityType.EXPLOSION, ExplosionFactory.CHARGED_SHOT_EXPLOSION);
        ObjectMap<String, Object> data = new ObjectMap<>() {{
            put(ConstKeys.OWNER, owner);
            put(ConstKeys.DIR, facing);
            put(ConstKeys.BOOL, fullyCharged);
        }};
        game.getGameEngine().spawnEntity(e, ShapeUtils.getCenterPoint(body.bounds), data);
    }

    @Override
    public void hitShield(Fixture shieldFixture) {
        owner = shieldFixture.entity;
        swapFacing();
        traj.x *= -1f;
        String reflectDir = shieldFixture.getUserData(ConstKeys.REFLECT, String.class);
        if (reflectDir.equals(ConstKeys.UP)) {
            traj.y = 5f * WorldVals.PPM;
        } else if (reflectDir.equals(ConstKeys.DOWN)) {
            traj.y = -5f * WorldVals.PPM;
        } else {
            traj.y = 0f;
        }
        getComponent(SoundComponent.class).request(SoundAsset.DINK_SOUND);
    }

    @Override
    public void onDamageInflictedTo(Damageable damageable) {
        dead = true;
        game.getGameEngine().spawnEntity(
                game.getEntityFactories().fetch(EntityType.EXPLOSION, ExplosionFactory.CHARGED_SHOT_EXPLOSION),
                body.bounds,
                new ObjectMap<>() {{
                    put(ConstKeys.DIR, facing);
                    put(ConstKeys.OWNER, owner);
                    put(ConstKeys.BOOL, fullyCharged);
                }});
    }

    private ShapeComponent shapeComponent() {
        return new ShapeComponent(new ShapeHandle(body.bounds));
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> body.velocity.set(traj));
    }

    private SpriteComponent spriteComponent() {
        SpriteHandle handle = new SpriteHandle(sprite, 4);
        handle.runnable = () -> {
            sprite.setFlip(is(Facing.LEFT), false);
            handle.setPosition(body.bounds, Position.CENTER);
        };
        return new SpriteComponent(handle);
    }

    private AnimationComponent animationComponent() {
        // fully charged anim
        TextureRegion fullyChargedRegion = game.getAssMan().getTextureRegion(MEGAMAN_CHARGED_SHOT, "Shoot");
        Animation fullyChargedAnim = new Animation(fullyChargedRegion, 2, .05f);
        // half charged anim
        TextureRegion halfChargedRegion = game.getAssMan().getTextureRegion(MEGAMAN_HALF_CHARGED_SHOT, "Shoot");
        Animation halfChargedAnim = new Animation(halfChargedRegion, 2, .05f);
        // anims
        ObjectMap<String, Animation> anims = new ObjectMap<>();
        anims.put("charged", fullyChargedAnim);
        anims.put("half", halfChargedAnim);
        // animator
        Animator animator = new Animator(sprite, () -> fullyCharged ? "charged" : "half", anims);
        return new AnimationComponent(animator);
    }

    private void defineBody() {
        float size = WorldVals.PPM;
        if (!fullyCharged) {
            size /= 2f;
        }
        body.bounds.setSize(size, size);
        // projectile fixture
        Fixture projectileFixture = new Fixture(this, FixtureType.PROJECTILE);
        body.fixtures.add(projectileFixture);
        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER);
        body.fixtures.add(damagerFixture);
    }

}
