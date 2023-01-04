package com.megaman.game.entities.explosions.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
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
import com.megaman.game.entities.Damageable;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;

public class SnowballExplosion extends Entity implements Damager {

    private static final float DUR = .075f;

    private static TextureRegion snowballExplosionReg;

    private final Body body;
    private final Sprite sprite;
    private final Timer durTimer;

    private ObjectSet<Class<? extends Damageable>> mask;

    public SnowballExplosion(MegamanGame game) {
        super(game, EntityType.EXPLOSION);
        if (snowballExplosionReg == null) {
            snowballExplosionReg = game.getAssMan().getTextureRegion(TextureAsset.EXPLOSIONS_1, "SnowballExplode");
        }
        sprite = new Sprite();
        mask = new ObjectSet<>();
        durTimer = new Timer(DUR);
        body = new Body(BodyType.ABSTRACT);
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(updatableComponent());
        putComponent(animationComponent());
    }


    @Override
    public boolean canDamage(Damageable damageable) {
        return mask.contains(damageable.getClass());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        body.bounds.setCenter(spawn);
        durTimer.reset();
        mask = data.containsKey(ConstKeys.MASK) ?
                (ObjectSet<Class<? extends Damageable>>) data.get(ConstKeys.MASK) : new ObjectSet<>();
    }

    private BodyComponent bodyComponent() {
        body.bounds.setSize(WorldVals.PPM);

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Rectangle(body.bounds));
        body.add(damagerFixture);

        return new BodyComponent(body);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            durTimer.update(delta);
            if (durTimer.isFinished()) {
                dead = true;
            }
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(2.5f * WorldVals.PPM, 2.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> h.setPosition(body.bounds, Position.CENTER);
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(snowballExplosionReg, 3, .025f, false));
    }

}
