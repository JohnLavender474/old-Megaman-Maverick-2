package com.megaman.game.entities.impl.explosions.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.entities.damage.Damager;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;

import static com.megaman.game.assets.TextureAsset.EXPLOSIONS_1;

public class PreciousExplosion extends Entity implements Damager {

    private static final float CULL_DUR = .15f;

    private static TextureRegion precExReg;

    private final Body body;
    private final Sprite sprite;
    private final Timer cullTimer;

    private Fixture damagerFixture;
    private boolean small;

    public PreciousExplosion(MegamanGame game) {
        super(game, EntityType.EXPLOSION);
        if (precExReg == null) {
            precExReg = game.getAssMan().getTextureRegion(EXPLOSIONS_1, "PreciousExplosion");
        }
        sprite = new Sprite();
        cullTimer = new Timer(CULL_DUR);
        body = new Body(BodyType.ABSTRACT);
        defineBody();
        putComponent(new BodyComponent(body));
        putComponent(spriteComponent());
        putComponent(updatableComponent());
        putComponent(animationComponent());
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        cullTimer.reset();
        small = data.containsKey(ConstKeys.BOOL) && (boolean) data.get(ConstKeys.BOOL);
        float spriteSize = small ? .5f : 1.25f;
        sprite.setSize(spriteSize * WorldVals.PPM, spriteSize * WorldVals.PPM);
        sprite.setCenter(spawn.x, spawn.y);
        float bodySize = small ? .5f : 1f;
        body.setSize(bodySize * WorldVals.PPM);
        body.setCenter(spawn);
        ((Rectangle) damagerFixture.shape).setSize(bodySize * WorldVals.PPM);
    }

    private void defineBody() {
        Array<ShapeHandle> h = new Array<>();

        // damagerFixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Rectangle());
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);
        this.damagerFixture = damagerFixture;

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }
    }

    private SpriteComponent spriteComponent() {
        return new SpriteComponent(new SpriteHandle(sprite, 4));
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(precExReg, 3, .05f, false));
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            cullTimer.update(delta);
            if (cullTimer.isFinished()) {
                dead = true;
            }
        });
    }

}
