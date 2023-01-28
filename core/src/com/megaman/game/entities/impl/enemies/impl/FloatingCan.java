package com.megaman.game.entities.impl.enemies.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.utils.damage.DamageNegotiation;
import com.megaman.game.entities.utils.damage.Damager;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.entities.impl.explosions.impl.ChargedShotExplosion;
import com.megaman.game.entities.impl.projectiles.impl.Bullet;
import com.megaman.game.entities.impl.projectiles.impl.ChargedShot;
import com.megaman.game.entities.impl.projectiles.impl.Fireball;
import com.megaman.game.health.HealthVals;
import com.megaman.game.pathfinding.PathfindParams;
import com.megaman.game.pathfinding.PathfindingComponent;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.*;

import java.util.HashMap;
import java.util.Map;

public class FloatingCan extends Enemy {

    private static final float SPEED = 1.5f;

    private static TextureRegion floatingCanReg;

    private final Sprite sprite;

    public FloatingCan(MegamanGame game) {
        super(game, BodyType.ABSTRACT);
        if (floatingCanReg == null) {
            floatingCanReg = game.getAssMan().getTextureRegion(TextureAsset.ENEMIES_1, "FloatingCan");
        }
        defineBody();
        sprite = new Sprite();
        putComponent(spriteComponent());
        putComponent(animationComponent());
        putComponent(pathfindingComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getCenterPoint(bounds);
        body.bounds.setCenter(spawn);
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDmgNegs() {
        return new HashMap<>() {{
            put(Bullet.class, new DamageNegotiation(10));
            put(Fireball.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShot.class, new DamageNegotiation(HealthVals.MAX_HEALTH));
            put(ChargedShotExplosion.class, new DamageNegotiation(15));
        }};
    }

    protected void defineBody() {
        body.bounds.setSize(.75f * WorldVals.PPM);
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE,
                new Rectangle().setSize(.75f * WorldVals.PPM));
        body.add(damageableFixture);
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER,
                new Rectangle().setSize(.75f * WorldVals.PPM));
        body.add(damagerFixture);
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> body.velocity.set(
                getComponent(PathfindingComponent.class).currentTrajectory.cpy().scl(WorldVals.PPM)));
    }

    private PathfindingComponent pathfindingComponent() {
        return new PathfindingComponent(new PathfindParams(
                this, body, () -> ShapeUtils.getTopCenterPoint(game.getMegaman().body.bounds),
                f -> f.fixtureType == FixtureType.BLOCK ||
                        (f.fixtureType == FixtureType.BODY && f.entity instanceof FloatingCan),
                r -> body.bounds.overlaps(r), SPEED, true));
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> {
            h.setPosition(body.bounds, Position.CENTER);
            sprite.setFlip(getComponent(PathfindingComponent.class).currentTrajectory.x < 0f, false);
            h.hidden = dmgBlink;
        };
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        return new AnimationComponent(sprite, new Animation(floatingCanReg, 4, .15f));
    }

}
