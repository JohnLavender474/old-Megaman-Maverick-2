package com.megaman.game.world;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Updatable;

public class Body implements Updatable {

    public static final float MIN_VEL = .01f;
    public static final float STANDARD_RESISTANCE_X = 1.035f;
    public static final float STANDARD_RESISTANCE_Y = 1.025f;

    public Rectangle bounds;
    public boolean[] senses;
    public BodyType bodyType;
    public Array<Fixture> fixtures;

    public Vector2 gravity;
    public Vector2 velocity;
    public Vector2 friction;
    public Vector2 velClamp;
    public Vector2 resistance;

    public boolean gravityOn;
    public boolean affectedByResistance;

    private final Vector2 prevPos;

    public Body(BodyType bodyType) {
        this.bodyType = bodyType;
        this.prevPos = new Vector2();
        this.gravity = new Vector2();
        this.bounds = new Rectangle();
        this.fixtures = new Array<>();
        this.velocity = new Vector2();
        this.friction = new Vector2();
        this.senses = new boolean[BodySense.values().length];
        this.velClamp = new Vector2(Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.resistance = new Vector2(STANDARD_RESISTANCE_X, STANDARD_RESISTANCE_Y);
    }

    public static boolean intersect(Body b1, Body b2, Rectangle overlap) {
        return Intersector.intersectRectangles(b1.bounds, b2.bounds, overlap);
    }

    public boolean is(BodySense sense) {
        return senses[sense.ordinal()];
    }

    public void set(BodySense sense, boolean is) {
        senses[sense.ordinal()] = is;
    }

    public boolean intersects(Body body, Rectangle overlap) {
        return Body.intersect(this, body, overlap);
    }

    public boolean overlaps(Body body) {
        return bounds.overlaps(body.bounds);
    }

    public void setPos(Vector2 pos, Position position) {
        ShapeUtils.setToPoint(bounds, pos, position);
    }

    public Vector2 getPos() {
        return new Vector2(bounds.x, bounds.y);
    }

    public void setPrevPos(Vector2 prevPos) {
        this.prevPos.set(prevPos);
    }

    public Vector2 getPosDelta() {
        return getPos().sub(prevPos);
    }

    public Vector2 getCenter() {
        return ShapeUtils.getCenterPoint(bounds);
    }

    @Override
    public void update(float delta) {
        // if at or below min, then set to zero
        if (Math.abs(velocity.x) <= MIN_VEL * WorldVals.PPM) {
            velocity.x = 0f;
        }
        if (Math.abs(velocity.y) <= MIN_VEL * WorldVals.PPM) {
            velocity.y = 0f;
        }
        // apply resistance
        if (affectedByResistance) {
            if (resistance.x > 0f) {
                velocity.x /= resistance.x;
            }
            if (resistance.y > 0f) {
                velocity.y /= resistance.y;
            }
        }
        resistance.set(STANDARD_RESISTANCE_X, STANDARD_RESISTANCE_Y);
        // apply gravity
        if (gravityOn) {
            velocity.add(gravity);
        }
        // clamp velocity
        if (velocity.x > 0f && velocity.x > Math.abs(velClamp.x)) {
            velocity.x = Math.abs(velClamp.x);
        } else if (velocity.x < 0f && velocity.x < -Math.abs(velClamp.x)) {
            velocity.x = -Math.abs(velClamp.x);
        }
        if (velocity.y > 0f && velocity.y > Math.abs(velClamp.y)) {
            velocity.y = Math.abs(velClamp.y);
        } else if (velocity.y < 0f && velocity.y < -Math.abs(velClamp.y)) {
            velocity.y = -Math.abs(velClamp.y);
        }
        // move bounds
        bounds.x += velocity.x * delta;
        bounds.y += velocity.y * delta;
        // move fixtures
        for (Fixture f : fixtures) {
            Vector2 p = ShapeUtils.getCenterPoint(bounds).add(f.offset);
            f.bounds.setCenter(p);
        }
    }

}
