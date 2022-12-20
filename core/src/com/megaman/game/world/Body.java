package com.megaman.game.world;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Updatable;

public class Body implements Updatable {

    public static final float STANDARD_RESISTANCE_X = 1.035f;
    public static final float STANDARD_RESISTANCE_Y = 1.025f;

    public static boolean intersect(Body b1, Body b2, Rectangle overlap) {
        return Intersector.intersectRectangles(b1.bounds, b2.bounds, overlap);
    }

    public BodyType bodyType;
    public Rectangle bounds = new Rectangle();
    public Array<Fixture> fixtures = new Array<>();
    public boolean[] senses = new boolean[BodySense.values().length];

    public Vector2 gravity = new Vector2();
    public Vector2 velocity = new Vector2();
    public Vector2 velClamp = new Vector2();
    public Vector2 friction = new Vector2();
    public Vector2 resistance = new Vector2(STANDARD_RESISTANCE_X, STANDARD_RESISTANCE_Y);

    public boolean gravityOn;
    public boolean affectedByResistance;

    private final Vector2 prevPos = new Vector2();

    public Body(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public boolean is(BodySense sense) {
        return senses[sense.ordinal()];
    }

    public void set(BodySense sense, boolean is) {
        senses[sense.ordinal()] = is;
    }

    public boolean overlaps(Body body) {
        return bounds.overlaps(body.bounds);
    }

    public void setPos(Vector2 pos, Position position) {
        ShapeUtils.setToPoint(bounds, pos, position);
    }

    public void setPrevPos(Vector2 prevPos) {
        this.prevPos.set(prevPos);
    }

    public Vector2 getPosDelta() {
        return new Vector2(bounds.x, bounds.y).sub(prevPos);
    }

    public Vector2 getCenter() {
        return ShapeUtils.getCenterPoint(bounds);
    }

    @Override
    public void update(float delta) {
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
        // resize body center
        bounds.x += velocity.x * delta * WorldConstVals.PPM;
        bounds.y += velocity.y * delta * WorldConstVals.PPM;
        // resize fixture positions
        fixtures.forEach(f -> {
            Vector2 p = ShapeUtils.getCenterPoint(bounds).add(f.offset);
            f.bounds.setCenter(p);
        });
        // reset resistance
        resistance.set(1f, 1f);
    }

}
