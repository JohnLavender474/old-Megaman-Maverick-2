package com.megaman.game.entities.enemies.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.animations.Animator;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.pathfinding.PathfindParams;
import com.megaman.game.pathfinding.PathfindingComponent;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Bat extends Enemy {

    private static final Logger logger = new Logger(Bat.class, MegamanGame.DEBUG && false);

    private static final float HANG_DUR = 1.75f;
    private static final float RELEASE_FROM_PERCH_DUR = .25f;

    private static final float DAMAGE_DUR = .05f;

    private static final float FLY_TO_ATTACK_SPEED = 3f;
    private static final float FLY_TO_RETREAT_SPEED = 8f;

    @RequiredArgsConstructor
    public enum BatStatus {

        HANGING("Hang"),
        OPEN_EYES("OpenEyes"),
        OPEN_WINGS("OpenWings"),
        FLYING_TO_ATTACK("Fly"),
        FLYING_TO_RETREAT("Fly");

        public final String regionName;

    }

    private final Sprite sprite;
    private final Timer hangTimer;
    private final Timer releasePerchTimer;

    private Fixture damageableFixture;
    private Fixture shieldFixture;
    private BatStatus currStat;

    public Bat(MegamanGame game) {
        super(game, DAMAGE_DUR, BodyType.DYNAMIC);
        sprite = new Sprite();
        hangTimer = new Timer(HANG_DUR);
        releasePerchTimer = new Timer(RELEASE_FROM_PERCH_DUR);
        putComponent(spriteComponent());
        putComponent(animationComponent());
        putComponent(pathfindingComponent());
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        hangTimer.reset();
        releasePerchTimer.reset();
        currStat = BatStatus.HANGING;
        Vector2 spawn = ShapeUtils.getCenterPoint(bounds);
        ShapeUtils.setTopCenterToPoint(body.bounds, spawn);
    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    @Override
    protected void defineBody(Body body) {
        body.bounds.setSize(.5f * WorldVals.PPM, .25f * WorldVals.PPM);
        Fixture headFixture = new Fixture(this, FixtureType.HEAD,
                new Rectangle().setSize(.5f * WorldVals.PPM, .175f * WorldVals.PPM));
        headFixture.offset.y = .375f * WorldVals.PPM;
        body.fixtures.add(headFixture);
        Rectangle m = new Rectangle().setSize(.75f * WorldVals.PPM);
        Fixture damageableFixture = new Fixture(this, FixtureType.DAMAGEABLE, new Rectangle(m));
        body.fixtures.add(damageableFixture);
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER, new Rectangle(m));
        body.fixtures.add(damagerFixture);
        this.damageableFixture = damageableFixture;
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD, new Rectangle(m));
        body.fixtures.add(shieldFixture);
        this.shieldFixture = shieldFixture;
        Fixture scannerFixture = new Fixture(this, FixtureType.SCANNER, new Rectangle(m));
        Consumer<Fixture> scanner = f -> {
            if (f.fixtureType == FixtureType.DAMAGEABLE && f.entity.equals(game.getMegaman())) {
                currStat = BatStatus.FLYING_TO_RETREAT;
            }
        };
        scannerFixture.putUserData(ConstKeys.CONSUMER, scanner);
        body.fixtures.add(scannerFixture);
    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);
        c.add(delta -> {
            switch (currStat) {
                case HANGING -> {
                    hangTimer.update(delta);
                    if (hangTimer.isFinished()) {
                        currStat = BatStatus.OPEN_EYES;
                        hangTimer.reset();
                    }
                }
                case OPEN_EYES, OPEN_WINGS -> {
                    releasePerchTimer.update(delta);
                    if (releasePerchTimer.isFinished()) {
                        if (currStat == BatStatus.OPEN_EYES) {
                            currStat = BatStatus.OPEN_WINGS;
                            releasePerchTimer.reset();
                        } else {
                            currStat = BatStatus.FLYING_TO_ATTACK;
                        }
                    }
                }
                case FLYING_TO_RETREAT -> {
                    if (is(BodySense.HEAD_TOUCHING_BLOCK)) {
                        currStat = BatStatus.HANGING;
                    }
                }
            }
            shieldFixture.active = currStat.equals(BatStatus.HANGING);
            damageableFixture.active = !currStat.equals(BatStatus.HANGING);
            if (currStat.equals(BatStatus.FLYING_TO_ATTACK)) {
                Vector2 trajectory = getComponent(PathfindingComponent.class)
                        .currentTrajectory.cpy().scl(WorldVals.PPM);
                body.velocity.set(trajectory);
                logger.log("Curr pos: " + body.bounds.x + ", " + body.bounds.y);
                logger.log("Megaman pos: " + game.getMegaman().getPosition());
                logger.log("Trajectory: " + trajectory);
                logger.log("Path: " + getComponent(PathfindingComponent.class).currentPath);
            } else if (currStat.equals(BatStatus.FLYING_TO_RETREAT)) {
                body.velocity.set(0f, FLY_TO_RETREAT_SPEED * WorldVals.PPM);
            } else {
                body.velocity.setZero();
            }
        });
    }

    private PathfindingComponent pathfindingComponent() {
        return new PathfindingComponent(new PathfindParams(
                this, body, () -> ShapeUtils.getTopCenterPoint(game.getMegaman().body.bounds),
                f -> f.fixtureType == FixtureType.BLOCK ||
                        (f.fixtureType == FixtureType.BODY && f.entity instanceof Bat),
                r -> body.bounds.overlaps(r), FLY_TO_ATTACK_SPEED, false));
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 4);
        h.updatable = delta -> h.setPosition(body.bounds, Position.CENTER);
        return new SpriteComponent(h);
    }

    private AnimationComponent animationComponent() {
        TextureAtlas atlas = game.getAssMan().getTextureAtlas(TextureAsset.ENEMIES_1);
        Supplier<String> keySupplier = () -> currStat.regionName;
        return new AnimationComponent(new Animator(sprite, keySupplier, new ObjectMap<>() {{
            put("Hang", new Animation(atlas.findRegion("Bat/Hang")));
            put("Fly", new Animation(atlas.findRegion("Bat/Fly"), 2, .1f));
            put("OpenEyes", new Animation(atlas.findRegion("Bat/OpenEyes")));
            put("OpenWings", new Animation(atlas.findRegion("Bat/OpenWings")));
        }}));
    }

}
