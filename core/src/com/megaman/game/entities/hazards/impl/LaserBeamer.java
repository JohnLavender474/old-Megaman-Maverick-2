package com.megaman.game.entities.hazards.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.movement.rotatingline.RotatingLine;
import com.megaman.game.shapes.*;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.sprites.SpriteHandle;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.*;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

public class LaserBeamer extends Entity implements Damager {

    private static final Logger logger = new Logger(LaserBeamer.class, MegamanGame.DEBUG && false);

    private static final float[] CONTACT_RADII = new float[]{2f, 5f, 8f};
    private static final float SPEED = 2f;
    private static final float RADIUS = 10f;
    private static final float CONTACT_TIME = .05f;
    private static final float SWITCH_DIR_TIME = 1f;
    private static final float MIN_DEGREES = 200f;
    private static final float MAX_DEGREES = 340f;
    private static final float INIT_DEGREES = 270f;
    private static final float THICKNESS = WorldVals.PPM / 32f;

    private static TextureRegion laserBeamerReg;

    private final Body body;
    private final Sprite sprite;
    private final Timer contactTimer;
    private final Timer switchDirTimer;

    private Queue<Vector2> contactPoints;

    private Fixture laserFixture;
    private Fixture damagerFixture;

    private RotatingLine rotatingLine;
    private Polyline laser;
    private Circle contactGlow;
    private boolean clockwise;
    private int contactIndex;

    public LaserBeamer(MegamanGame game) {
        super(game, EntityType.HAZARD);
        if (laserBeamerReg == null) {
            laserBeamerReg = game.getAssMan().getTextureRegion(TextureAsset.HAZARDS_1, "LaserBeamer");
        }
        sprite = new Sprite(laserBeamerReg);
        body = new Body(BodyType.ABSTRACT);
        body.bounds.setSize(WorldVals.PPM);
        putComponent(bodyComponent());
        putComponent(spriteComponent());
        putComponent(updatableComponent());
        contactTimer = new Timer(CONTACT_TIME);
        switchDirTimer = new Timer(SWITCH_DIR_TIME, true);
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        Vector2 spawn = ShapeUtils.getCenterPoint((Rectangle) data.get(ConstKeys.SPAWN));
        body.bounds.setPosition(spawn);
        rotatingLine = new RotatingLine(spawn, RADIUS * WorldVals.PPM, SPEED * WorldVals.PPM, INIT_DEGREES);
        float[] v = rotatingLine.getPolyline().getVertices();
        laser = new Polyline(Arrays.copyOf(v, v.length));
        laser.setOrigin(spawn.x, spawn.y);
        laserFixture.shape = rotatingLine.getPolyline();
        damagerFixture.shape = laser;
        LineHandle l = new LineHandle();
        l.lineSupplier = () -> ShapeUtils.polylineToPointPair(laser);
        l.thicknessSupplier = () -> THICKNESS;
        l.shapeTypeSupplier = () -> ShapeRenderer.ShapeType.Filled;
        l.colorSupplier = () -> Color.RED;
        putComponent(new LineComponent(l));
        ShapeHandle s = new ShapeHandle(() -> contactGlow, () -> Color.WHITE);
        s.prioritySupplier = () -> 1;
        s.shapeTypeSupplier = () -> ShapeRenderer.ShapeType.Filled;
        putComponent(new ShapeComponent(s));
        contactTimer.reset();
        switchDirTimer.setToEnd();
        contactPoints = new PriorityQueue<>((p1, p2) -> Float.compare(p1.dst2(spawn), p2.dst2(spawn)));
        laserFixture.putUserData(ConstKeys.COLLECTION, contactPoints);
    }

    private BodyComponent bodyComponent() {
        ShapeComponent s = new ShapeComponent();
        putComponent(s);
        body.bounds.setSize(WorldVals.PPM, WorldVals.PPM);
        Fixture laserFixture = new Fixture(this, FixtureType.LASER);
        laserFixture.offset.y = WorldVals.PPM / 16f;
        body.fixtures.add(laserFixture);
        this.laserFixture = laserFixture;
        Fixture damagerFixture = new Fixture(this, FixtureType.DAMAGER);
        body.fixtures.add(damagerFixture);
        this.damagerFixture = damagerFixture;
        Fixture shieldFixture = new Fixture(this, FixtureType.SHIELD,
                new Rectangle().setSize(WorldVals.PPM, WorldVals.PPM * .85f));
        shieldFixture.offset.y = WorldVals.PPM / 2f;
        shieldFixture.putUserData(ConstKeys.DIR, ConstKeys.UP);
        body.fixtures.add(shieldFixture);
        return new BodyComponent(body);
    }

    private SpriteComponent spriteComponent() {
        sprite.setSize(1.5f * WorldVals.PPM, 1.5f * WorldVals.PPM);
        SpriteHandle h = new SpriteHandle(sprite, 1);
        h.updatable = delta -> h.setPosition(rotatingLine.getPos(), Position.BOTTOM_CENTER, 0f, -.06f * WorldVals.PPM);
        return new SpriteComponent(h);
    }

    private UpdatableComponent updatableComponent() {
        return new UpdatableComponent(delta -> {
            contactTimer.update(delta);
            if (contactTimer.isFinished()) {
                contactIndex++;
                contactTimer.reset();
            }
            if (contactIndex > 2) {
                contactIndex = 0;
            }
            Vector2 origin = rotatingLine.getPos();
            Vector2 endPos = rotatingLine.getEndPoint();
            contactGlow = new Circle();
            if (!contactPoints.isEmpty()) {
                Vector2 closestContact = contactPoints.poll();
                endPos.set(closestContact);
                contactGlow.setPosition(closestContact.x, closestContact.y);
                contactGlow.setRadius(CONTACT_RADII[contactIndex]);
            }
            contactPoints.clear();
            laser.setVertices(new float[]{origin.x, origin.y, endPos.x, endPos.y});
            switchDirTimer.update(delta);
            if (!switchDirTimer.isFinished()) {
                return;
            }
            if (switchDirTimer.isJustFinished()) {
                clockwise = !clockwise;
                float speed = SPEED * WorldVals.PPM;
                if (clockwise) {
                    speed *= -1f;
                }
                rotatingLine.setSpeed(speed);
            }
            rotatingLine.update(delta);
            logger.log("Pos: " + rotatingLine.getPos() +
                    ", End: " + rotatingLine.getEndPoint() +
                    ", Degrees: " + rotatingLine.getDegrees());
            if (clockwise && rotatingLine.getDegrees() <= MIN_DEGREES) {
                rotatingLine.setDegrees(MIN_DEGREES);
                switchDirTimer.reset();
            } else if (!clockwise && rotatingLine.getDegrees() >= MAX_DEGREES) {
                rotatingLine.setDegrees(MAX_DEGREES);
                switchDirTimer.reset();
            }
        });
    }

}
