package com.megaman.game.entities.enemies.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Faceable;
import com.megaman.game.entities.Facing;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.projectiles.impl.Bullet;
import com.megaman.game.entities.projectiles.impl.ChargedShot;
import com.megaman.game.entities.projectiles.impl.Fireball;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PreciousJoe extends Enemy implements Faceable {

    /*
    TODO:
    - Stand
    - Form PreciousShot, using it as shield
    - Shoot PreciousShot
     */

    private static final float STAND_DUR = 1f;
    private static final float FORM_DUR = 1f;
    private static final float SHOOT_DUR = .15f;
    private static final float PRECIOUS_X = 15f;

    private final Sprite sprite;
    private final Timer standTimer;
    private final Timer formTimer;
    private final Timer shootTimer;

    @Getter
    @Setter
    private Facing facing;

    public PreciousJoe(MegamanGame game) {
        super(game, BodyType.DYNAMIC);
        sprite = new Sprite();
        standTimer = new Timer(STAND_DUR);
        formTimer = new Timer(FORM_DUR);
        shootTimer = new Timer(SHOOT_DUR);
        putComponent(spriteComponent());
        putComponent(animationComponent());
    }

    public boolean isStanding() {
        return !standTimer.isFinished();
    }

    public boolean isFormingThePrecious() {
        return !formTimer.isFinished();
    }

    public boolean isShooting() {
        return !shootTimer.isFinished();
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getBottomCenterPoint(bounds);
        ShapeUtils.setBottomCenterToPoint(body.bounds, spawn);
        standTimer.reset();
        formTimer.setToEnd();
        shootTimer.setToEnd();
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(5));
            put(Fireball.class, new DamageNegotiation(15));
            put(ChargedShot.class, new DamageNegotiation(damager ->
                    ((ChargedShot) damager).isFullyCharged() ? 15 : 10));
            put(ChargedShotExplosion.class, new DamageNegotiation(damager ->
                    ((ChargedShotExplosion) damager).isFullyCharged() ? 15 : 10));
        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.bounds.setSize(WorldVals.PPM, 1.25f * WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // damager fixture
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.75f * WorldVals.PPM, 1.15f * WorldVals.PPM));
        h.add(new ShapeHandle(damagerFixture.shape, Color.RED));
        body.add(damagerFixture);

        // damageable fixture
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(.8f * WorldVals.PPM, 1.35f * WorldVals.PPM));
        h.add(new ShapeHandle(damageableFixture.shape, Color.PURPLE));
        body.add(damageableFixture);

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            setFacing(game.getMegaman().body.isRightOf(body) ? Facing.RIGHT : Facing.LEFT);
            if (isStanding()) {
                standTimer.update(delta);
                if (standTimer.isFinished()) {

                    // TODO: create crystal, set crystal to "forming"

                    formTimer.reset();
                }
            } else if (isFormingThePrecious()) {
                formTimer.update(delta);
                if (formTimer.isFinished()) {

                    // TODO: shoot crystal

                    shootTimer.reset();
                }
            } else {
                shootTimer.update(delta);
                if (shootTimer.isFinished()) {
                    standTimer.reset();
                }
            }
        });
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.35f * WorldVals.PPM, 1.35f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.BOTTOM_CENTER);
            sprite.setFlip(is(Facing.LEFT), false);
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        Supplier<String> keySupplier = () -> {
            if (isStanding()) {
                return "Stand";
            } else if (isFormingThePrecious()) {
                return "Charge";
            } else {
                return "Shoot";
            }
        };
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        return new AnimationComponent(sprite, keySupplier, new ObjectMap<>() {{
            put("Stand", new Animation(atlas.findRegion("Stand")));
            put("Charge", new Animation(atlas.findRegion("Charge"), 2, .15f));
            put("Shoot", new Animation(atlas.findRegion("Shoot")));
        }});
    }

}
