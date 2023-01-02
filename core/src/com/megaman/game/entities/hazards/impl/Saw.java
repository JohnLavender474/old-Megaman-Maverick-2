package com.megaman.game.entities.hazards.impl;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
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
import com.megaman.game.shapes.*;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Pair;
import com.megaman.game.world.*;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.Color.*;
import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled;

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
        LineHandle lineHandle = new LineHandle();
        lineHandle.setLineSupplier(() -> Pair.of(p.getAnchor(), p.getEnd()));
        lineHandle.setColorSupplier(() -> DARK_GRAY);
        lineHandle.setThicknessSupplier(() -> WorldVals.PPM / 8f);
        lineHandle.setShapeTypeSupplier(() -> Filled);
        putComponent(new LineComponent(lineHandle));
        Circle circle1 = new Circle(p.getAnchor(), WorldVals.PPM / 4f);
        Circle circle2 = new Circle();
        circle2.setRadius(WorldVals.PPM / 4f);
        Array<ShapeHandle> shapeHandles = new Array<>();
        ShapeHandle shapeHandle1 = new ShapeHandle();
        shapeHandle1.setShapeSupplier(() -> circle1);
        shapeHandle1.setShapeTypeSupplier(() -> Filled);
        shapeHandle1.setColorSupplier(() -> DARK_GRAY);
        shapeHandles.add(shapeHandle1);
        ShapeHandle shapeHandle2 = new ShapeHandle();
        shapeHandle2.setShapeSupplier(() -> circle2);
        shapeHandle2.setColorSupplier(() -> DARK_GRAY);
        shapeHandle2.setShapeTypeSupplier(() -> Filled);
        shapeHandle2.setUpdatable(delta -> circle2.setPosition(p.getEnd()));
        shapeHandles.add(shapeHandle2);
        putComponent(new ShapeComponent(shapeHandles));

    }

    private void setToRotation(Vector2 spawn) {
        RotatingLine r = new RotatingLine(spawn, LENGTH * WorldVals.PPM, ROTATION_SPEED * WorldVals.PPM);
        putComponent(new RotatingLineComponent(r, delta -> body.bounds.setCenter(r.getEndPoint())));
        LineHandle lineHandle = new LineHandle();
        lineHandle.setLineSupplier(() -> Pair.of(r.getPos(), r.getEndPoint()));
        lineHandle.setColorSupplier(() -> DARK_GRAY);
        lineHandle.setThicknessSupplier(() -> WorldVals.PPM / 8f);
        lineHandle.setShapeTypeSupplier(() -> Filled);
        putComponent(new LineComponent(lineHandle));
        Circle circle1 = new Circle();
        circle1.setRadius(WorldVals.PPM / 4f);
        Circle circle2 = new Circle();
        circle2.setRadius(WorldVals.PPM / 4f);
        List<ShapeHandle> shapeHandles = new ArrayList<>();
        ShapeHandle shapeHandle1 = new ShapeHandle();
        shapeHandle1.setShapeSupplier(() -> circle1);
        shapeHandle1.setShapeTypeSupplier(() -> Filled);
        shapeHandle1.setColorSupplier(() -> DARK_GRAY);
        shapeHandle1.setUpdatable(delta -> circle1.setPosition(r.getPos()));
        shapeHandles.add(shapeHandle1);
        ShapeHandle shapeHandle2 = new ShapeHandle();
        shapeHandle2.setShapeSupplier(() -> circle2);
        shapeHandle2.setColorSupplier(() -> DARK_GRAY);
        shapeHandle2.setShapeTypeSupplier(() -> Filled);
        shapeHandle2.setUpdatable(delta -> circle2.setPosition(r.getEndPoint()));
        shapeHandles.add(shapeHandle2);
        putComponent(new ShapeComponent(shapeHandles));
    }

    private void setToTrajectory(String trajStr, Vector2 spawn) {
        // putComponent(new TrajectoryComponent(body, trajStr, spawn));
        putComponent(new TrajectoryComponent(body, trajStr));
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
        body.bounds.setSize(2f * WorldVals.PPM, 2f * WorldVals.PPM);
        Array<ShapeHandle> h = new Array<>();

        // death fixture
        Circle deathCircle = new Circle();
        deathCircle.radius = .95f * WorldVals.PPM;
        Fixture deathFixture = new Fixture(this, FixtureType.DEATH, deathCircle);
        body.add(deathFixture);
        h.add(new ShapeHandle(deathCircle, RED));

        // TODO: test circle death fixture
        /*
        // death fixture 1
        Fixture deathFixture1 = new Fixture(this, FixtureType.DEATH,
                new Rectangle().setSize(2f * WorldVals.PPM, WorldVals.PPM));
        body.add(deathFixture1);
        h.add(new ShapeHandle(deathFixture1.shape, Color.RED));

        // death fixture 2
        Fixture deathFixture2 = new Fixture(this, FixtureType.DEATH,
                new Rectangle().setSize(WorldVals.PPM, 2f * WorldVals.PPM));
        body.add(deathFixture2);
        h.add(new ShapeHandle(deathFixture2.shape, Color.RED));
         */

        // shield fixture
        Circle shieldCircle = new Circle();
        shieldCircle.radius = WorldVals.PPM;
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD, shieldCircle);
        shieldFixture.putUserData(ConstKeys.REFLECT, ConstKeys.UP);
        body.add(shieldFixture);
        h.add(new ShapeHandle(shieldCircle, PURPLE));

        // TODO: test circle shield fixture
        /*
        // shield fixture 1
        Fixture shieldFixture1 = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(WorldVals.PPM));
        shieldFixture1.offset.y = WorldVals.PPM / 2f;
        shieldFixture1.putUserData(ConstKeys.REFLECT, ConstKeys.UP);
        body.add(shieldFixture1);
        h.add(new ShapeHandle(shieldFixture1.shape, Color.PURPLE));

        // shield fixture 2
        Fixture shieldFixture2 = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(WorldVals.PPM));
        shieldFixture2.offset.y = -WorldVals.PPM / 2f;
        shieldFixture2.putUserData(ConstKeys.REFLECT, ConstKeys.DOWN);
        body.add(shieldFixture2);
        h.add(new ShapeHandle(shieldFixture2.shape, Color.PURPLE));
         */

        if (MegamanGame.DEBUG) {
            putComponent(new ShapeComponent(h));
        }

        return new BodyComponent(body);
    }

}
