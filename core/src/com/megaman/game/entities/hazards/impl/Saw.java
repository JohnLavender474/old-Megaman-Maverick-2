package com.megaman.game.entities.hazards.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.movement.pendulum.Pendulum;
import com.megaman.game.movement.pendulum.PendulumComponent;
import com.megaman.game.movement.rotatingline.RotatingLine;
import com.megaman.game.movement.rotatingline.RotatingLineComponent;
import com.megaman.game.movement.trajectory.TrajectoryComponent;
import com.megaman.game.shapes.ShapeComponent;
import com.megaman.game.shapes.ShapeHandle;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.world.*;

public class Saw extends Entity {

    public static final String PENDULUM_TYPE = "P";
    public static final String ROTATION_TYPE = "R";
    public static final String TRAJECTORY_TYPE = "T";

    private static final float LENGTH = 3f;
    private static final float ROTATION_SPEED = 2f;
    private static final float PENDULUM_GRAVITY = 10f;

    private static TextureRegion sawReg;

    private final Body body;
    private final Sprite sprite;

    public Saw(MegamanGame game) {
        super(game, EntityType.HAZARD);
        if (sawReg == null) {
            sawReg = game.getAssMan().getTextureRegion(TextureAsset.HAZARDS_1, "Saw");
        }
        sprite = new Sprite();
        body = new Body(BodyType.ABSTRACT);
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(animationComponent());

    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        removeComponent(PendulumComponent.class);
        removeComponent(TrajectoryComponent.class);
        removeComponent(RotatingLineComponent.class);
        Vector2 spawn = ShapeUtils.getCenterPoint((Rectangle) data.get(ConstKeys.SPAWN));
        String type = (String) data.get(ConstKeys.TYPE);
        switch (type) {
            case PENDULUM_TYPE -> setToPendulum(spawn);
            case ROTATION_TYPE -> setToRotation(spawn);
            case TRAJECTORY_TYPE -> setToTrajectory((String) data.get(ConstKeys.TRAJECTORY), spawn);
            default -> throw new IllegalStateException("Incompatible type for Saw: " + type);
        }
    }

    private void setToPendulum(Vector2 spawn) {
        Pendulum p = new Pendulum(LENGTH * WorldVals.PPM, PENDULUM_GRAVITY * WorldVals.PPM, spawn);
        putComponent(new PendulumComponent(p, delta -> body.bounds.setCenter(p.getEnd())));

        // TODO: add sprite (animated?) for pendulum
        /*
        Animator animator = new Animator(null, null);
        runOnDeath.add(() -> getComponent(AnimationComponent.class).animators.removeValue(animator, true));
        */

    }

    private void setToRotation(Vector2 spawn) {
        RotatingLine r = new RotatingLine(spawn, LENGTH * WorldVals.PPM, ROTATION_SPEED * WorldVals.PPM);
        putComponent(new RotatingLineComponent(r, delta -> body.bounds.setCenter(r.getEndPoint())));

        // TODO: add sprite (animated?) for chain
        /*
        Animator animator = new Animator(null, null);
        runOnDeath.add(() -> getComponent(AnimationComponent.class).animators.removeValue(animator, true));
        */

    }

    private void setToTrajectory(String trajStr, Vector2 spawn) {
        putComponent(new TrajectoryComponent(body, trajStr, spawn));
    }

    private AnimationComponent animationComponent() {
        Animation anim = new Animation(sawReg, 2, .1f);
        return new AnimationComponent(sprite, anim);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(2f * WorldVals.PPM, 2f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 10);
        h.updatable = delta -> h.setPosition(body.bounds, Position.CENTER);
        return new SpriteComponent(h);
    }

    private BodyComponent bodyComponent() {
        ShapeComponent s = new ShapeComponent();
        putComponent(s);
        body.bounds.setSize(2f * WorldVals.PPM, 2f * WorldVals.PPM);
        Fixture deathFixture1 = new Fixture(this, FixtureType.DEATH,
                new Rectangle().setSize(2f * WorldVals.PPM, WorldVals.PPM));
        body.fixtures.add(deathFixture1);
        s.shapeHandles.add(new ShapeHandle(deathFixture1.shape, Color.RED));
        Fixture deathFixture2 = new Fixture(this, FixtureType.DEATH,
                new Rectangle().setSize(WorldVals.PPM, 2f * WorldVals.PPM));
        body.fixtures.add(deathFixture2);
        s.shapeHandles.add(new ShapeHandle(deathFixture2.shape, Color.RED));
        Fixture shieldFixture1 = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(WorldVals.PPM));
        shieldFixture1.offset.y = WorldVals.PPM / 2f;
        shieldFixture1.putUserData(ConstKeys.REFLECT, ConstKeys.UP);
        body.fixtures.add(shieldFixture1);
        s.shapeHandles.add(new ShapeHandle(shieldFixture1.shape, Color.PURPLE));
        Fixture shieldFixture2 = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(WorldVals.PPM));
        shieldFixture2.offset.y = -WorldVals.PPM / 2f;
        shieldFixture2.putUserData(ConstKeys.REFLECT, ConstKeys.DOWN);
        body.fixtures.add(shieldFixture2);
        s.shapeHandles.add(new ShapeHandle(shieldFixture2.shape, Color.PURPLE));
        return new BodyComponent(body);
    }

}
