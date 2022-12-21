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
import com.megaman.game.entities.*;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.BodyComponent;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;
import lombok.Getter;
import lombok.Setter;

import static com.megaman.game.assets.TextureAsset.MEGAMAN_CHARGED_SHOT;
import static com.megaman.game.assets.TextureAsset.MEGAMAN_HALF_CHARGED_SHOT;

public class ChargedShot extends Projectile implements Faceable {

    private final Sprite sprite = new Sprite();
    private final Vector2 traj = new Vector2();

    private boolean fullyCharged;
    @Getter
    @Setter
    private Facing facing;

    public ChargedShot(MegamanGame game, Entity owner) {
        super(game, owner);
        addComponent(bodyComponent());
        addComponent(spriteComponent());
        addComponent(animationComponent());
        addComponent(updatableComponent());
    }

    @Override
    public void init(Vector2 center, ObjectMap<String, Object> data) {
        // owner
        owner = (Entity) data.get(ConstKeys.OWNER);
        // fully charged?
        fullyCharged = (boolean) data.get(ConstKeys.BOOL);
        // set bounds of sprite, body, and fixtures
        float bodyDim = WorldVals.PPM;
        float spriteDim = WorldVals.PPM;
        if (fullyCharged) {
            spriteDim *= 1.75f;
        } else {
            bodyDim /= 2f;
            spriteDim *= 1.25f;
        }
        sprite.setSize(spriteDim, spriteDim);
        sprite.setCenter(center.x, center.y);
        body.bounds.setSize(bodyDim, bodyDim);
        body.bounds.setCenter(center);
        body.fixtures.forEach(f -> f.bounds.set(body.bounds));
        // trajectory
        traj.set((Vector2) data.get(ConstKeys.TRAJECTORY));
        // set facing
        facing = traj.x > 0f ? Facing.RIGHT : Facing.LEFT;
    }

    @Override
    public void onDamageInflictedTo(Damageable damageable) {
        dead = true;
        game.getGameEngine().spawnEntity(
                game.getEntityFactories().fetch(EntityType.EXPLOSION, "ChargedShotExplosion"),
                body.bounds,
                new ObjectMap<>() {{
                    put(ConstKeys.DIR, facing);
                    put(ConstKeys.OWNER, owner);
                    put(ConstKeys.BOOL, fullyCharged);
                }});
        // TODO: Change to pool
        // game.getGameEngine().spawnEntity(new ChargedShotExplosion(game, body, is(Facing.LEFT), fullyCharged));
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
        // animator
        Animator animator = new Animator(sprite, () -> fullyCharged ? "charged" : "half",
                key -> key.equals("charged") ? fullyChargedAnim : halfChargedAnim);
        return new AnimationComponent(animator);
    }

    private BodyComponent bodyComponent() {
        // projectile fixture
        Fixture projectileFixture = new Fixture(this, FixtureType.PROJECTILE);
        body.fixtures.add(projectileFixture);
        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER);
        body.fixtures.add(damagerFixture);
        return new BodyComponent(body);
    }

}
