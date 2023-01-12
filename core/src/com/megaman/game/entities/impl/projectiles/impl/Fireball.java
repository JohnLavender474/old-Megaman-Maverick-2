package com.megaman.game.entities.impl.projectiles.impl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.entities.damage.Damageable;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.impl.decorations.DecorationFactory;
import com.megaman.game.entities.impl.decorations.impl.SmokePuff;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.impl.megaman.Megaman;
import com.megaman.game.entities.impl.projectiles.Projectile;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.UtilMethods;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.BodyType;
import com.megaman.game.world.Fixture;
import com.megaman.game.world.FixtureType;
import com.megaman.game.world.WorldVals;

public class Fireball extends Projectile {

    private static final Logger logger = new Logger(Fireball.class, MegamanGame.DEBUG);

    private static final float ROTATION = 1000f;
    private static final float GRAVITY = -.25f;
    private static final float CULL_DUR = 1f;
    private static final float Y_BOUNCE = 7.5f;
    private static final float X_VEL = 10f;

    private final Timer cullTimer;

    private float xVel;
    private boolean burst;

    public Fireball(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        cullTimer = new Timer(CULL_DUR);
        defineBody();
        putComponent(spriteComponent());
        putComponent(animationComponent());
        putComponent(updatableComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        super.init(spawn, data);
        burst = false;
        cullTimer.reset();
        body.gravityOn = true;
        xVel = X_VEL * WorldVals.PPM;
        if ((boolean) data.get(ConstKeys.LEFT)) {
            xVel *= -1f;
        }
        body.velocity.x = xVel;
        body.velocity.y = Y_BOUNCE * WorldVals.PPM;
    }

    private void burst() {
        burst = true;
        getComponent(SoundComponent.class).requestToPlay(SoundAsset.ATOMIC_FIRE_SOUND);
    }

    @Override
    public void hitBody(Fixture bodyFixture) {
        logger.log("Hit body");
        if (UtilMethods.mask(owner, bodyFixture.entity, o -> o instanceof Megaman, o -> o instanceof Enemy)) {
            burst();
        }
    }

    @Override
    public void hitBlock(Fixture blockFixture) {
        logger.log("Hit block");
        burst();
    }

    @Override
    public void hitShield(Fixture shieldFixture) {
        logger.log("Hit shield");
        xVel *= -1f;
        getComponent(SoundComponent.class).requestToPlay(SoundAsset.DINK_SOUND);
    }

    @Override
    public void hitWater(Fixture waterFixture) {
        logger.log("Hit water");
        dead = true;
        SmokePuff puff = (SmokePuff) game.getEntityFactories()
                .fetch(EntityType.DECORATION, DecorationFactory.SMOKE_PUFF);
        Rectangle r = ShapeUtils.getBoundingRect(waterFixture.shape);
        Vector2 pos = new Vector2(body.getCenter().x, r.y + r.height);
        game.getGameEngine().spawn(puff, pos);
        game.getAudioMan().play(SoundAsset.WHOOSH_SOUND);
    }

    @Override
    public void onDamageInflictedTo(Damageable damageable) {
        burst();
    }

    private void defineBody() {
        body.gravity.y = GRAVITY * WorldVals.PPM;
        body.bounds.setSize(.9f * WorldVals.PPM);

        // projectile fixture
        Fixture projFixture = new Fixture(this, FixtureType.PROJECTILE, new Rectangle().setSize(.9f * WorldVals.PPM));
        body.add(projFixture);

        // damager fixture
        Fixture dmgrFixture = new Fixture(this, FixtureType.DAMAGER, new Rectangle().setSize(.9f * WorldVals.PPM));
        body.add(dmgrFixture);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            if (burst) {
                body.velocity.x = 0f;
                cullTimer.update(delta);
            } else {
                body.velocity.x = xVel;
            }
            if (cullTimer.isFinished()) {
                dead = true;
            }
        });
    }

    private AnimationComponent animationComponent() {
        TextureAtlas fireballAtlas = game.getAssMan().getTextureAtlas(TextureAsset.PROJECTILES_1);
        TextureAtlas flameAtlas = game.getAssMan().getTextureAtlas(TextureAsset.HAZARDS_1);
        return new AnimationComponent(sprite, () -> burst ? "Flame" : "Fireball", new ObjectMap<>() {{
            put("Flame", new Animation(flameAtlas.findRegion("Flame"), 4, .1f));
            put("Fireball", new Animation(fireballAtlas.findRegion("Fire/Fireball")));
        }});
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.25f * WorldVals.PPM, 1.25f * WorldVals.PPM);
        sprite.setOrigin(sprite.getWidth() / 2f, sprite.getHeight() / 2f);
        SpriteHandle h = new SpriteHandle(sprite, 3);
        h.updatable = delta -> {
            if (burst) {
                sprite.setRotation(0f);
            } else {
                sprite.rotate(ROTATION * delta);
            }
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
        };
        return new SpriteComponent(h);
    }

}
