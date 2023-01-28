package com.megaman.game.world;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class Body implements Updatable, Resettable {

    public static final float MIN_VEL = .01f;
    public static final float STANDARD_RESISTANCE_X = 1.035f;
    public static final float STANDARD_RESISTANCE_Y = 1.025f;

    public Rectangle bounds;
    public boolean[] senses;
    public BodyType bodyType;
    public Array<Fixture> fixtures;
    public ObjectSet<String> labels;
    public ObjectMap<String, Object> userData;

    public Updatable preProcess;
    public Updatable postProcess;

    public Vector2 gravity;
    public Vector2 velocity;
    public Vector2 friction;
    public Vector2 velClamp;
    public Vector2 resistance;

    public boolean gravityOn;
    public boolean collisionOn;
    public boolean affectedByResistance;

    private final Vector2 prevPos;

    public Body(BodyType bodyType) {
        this(bodyType, false);
    }

    public Body(BodyType bodyType, boolean gravityOn) {
        this(bodyType, gravityOn, 0f, 0f);
    }

    public Body(BodyType bodyType, boolean gravityOn, float gravityX, float gravityY) {
        this.bodyType = bodyType;
        this.gravityOn = gravityOn;
        collisionOn = true;
        prevPos = new Vector2();
        bounds = new Rectangle();
        fixtures = new Array<>();
        velocity = new Vector2();
        friction = new Vector2();
        labels = new ObjectSet<>();
        userData = new ObjectMap<>();
        gravity = new Vector2(gravityX, gravityY);
        senses = new boolean[BodySense.values().length];
        velClamp = new Vector2(Integer.MAX_VALUE, Integer.MAX_VALUE);
        resistance = new Vector2(STANDARD_RESISTANCE_X, STANDARD_RESISTANCE_Y);
    }

    public boolean hasUserData(String key) {
        return userData.containsKey(key);
    }

    public void putUserData(String key, Object o) {
        userData.put(key, o);
    }

    public <T> T getUserData(String key, Class<T> tClass) {
        return tClass.cast(userData.get(key));
    }

    public void removeUserData(String key) {
        userData.remove(key);
    }

    public void add(Fixture fixture) {
        fixtures.add(fixture);
    }

    public boolean isRightOf(Body body) {
        return getCenter().x > body.getCenter().x;
    }

    public boolean isAbove(Body body) {
        return getCenter().y > body.getCenter().y;
    }

    public boolean is(BodyType bodyType) {
        return this.bodyType == bodyType;
    }

    public boolean isAny(BodyType... bodyTypes) {
        for (BodyType type : bodyTypes) {
            if (is(bodyType)) {
                return true;
            }
        }
        return false;
    }

    public boolean is(BodySense sense) {
        return senses[sense.ordinal()];
    }

    public boolean isAny(BodySense... bodySenses) {
        for (BodySense bodySense : bodySenses) {
            if (is(bodySense)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAll(BodySense... bodySenses) {
        for (BodySense bodySense : bodySenses) {
            if (!is(bodySense)) {
                return false;
            }
        }
        return true;
    }

    public void set(BodySense sense, boolean is) {
        senses[sense.ordinal()] = is;
    }

    public boolean intersects(Rectangle rect, Rectangle overlap) {
        return Intersector.intersectRectangles(bounds, rect, overlap);
    }

    public boolean intersects(Body body, Rectangle overlap) {
        return intersects(body.bounds, overlap);
    }

    public boolean overlaps(Rectangle rect) {
        return bounds.overlaps(rect);
    }

    public boolean overlaps(Body body) {
        return overlaps(body.bounds);
    }

    public void setWidth(float width) {
        bounds.setWidth(width);
    }

    public void setHeight(float height) {
        bounds.setHeight(height);
    }

    public void setSize(float sizeXY) {
        setSize(sizeXY, sizeXY);
    }

    public void setSize(float width, float height) {
        setWidth(width);
        setHeight(height);
    }

    public void setX(float x) {
        bounds.x = x;
    }

    public void setY(float y) {
        bounds.y = y;
    }

    public void setMaxX(float x) {
        bounds.x = x - bounds.width;
    }

    public void setMaxY(float y) {
        bounds.y = y - bounds.height;
    }

    public void setPos(Vector2 pos) {
        bounds.setPosition(pos);
    }

    public void setPos(Vector2 pos, Position position) {
        ShapeUtils.setToPoint(bounds, pos, position);
    }

    public float getX() {
        return bounds.x;
    }

    public float getY() {
        return bounds.y;
    }

    public float getMaxX() {
        return bounds.x + bounds.width;
    }

    public float getMaxY() {
        return bounds.y + bounds.height;
    }

    public Vector2 getPos() {
        return new Vector2(getX(), getY());
    }

    public void setCenter(Vector2 center) {
        setCenter(center.x, center.y);
    }

    public void setCenter(float x, float y) {
        setCenterX(x);
        setCenterY(y);
    }

    public void setCenterX(float x) {
        float centerY = getCenter().y;
        bounds.setCenter(x, centerY);
    }

    public void setCenterY(float y) {
        float centerX = getCenter().x;
        bounds.setCenter(centerX, y);
    }

    public Vector2 getCenter() {
        return ShapeUtils.getCenterPoint(bounds);
    }

    public void setPrevPos(float x, float y) {
        prevPos.set(x, y);
    }

    public Vector2 getPosDelta() {
        return getPos().sub(prevPos);
    }

    public void translate(float x, float y) {
        bounds.x += x;
        bounds.y += y;
    }

    @Override
    public void update(float delta) {
        if (Math.abs(velocity.x) <= MIN_VEL * WorldVals.PPM) {
            velocity.x = 0f;
        }
        if (Math.abs(velocity.y) <= MIN_VEL * WorldVals.PPM) {
            velocity.y = 0f;
        }
        if (affectedByResistance) {
            if (resistance.x > 0f) {
                velocity.x /= resistance.x;
            }
            if (resistance.y > 0f) {
                velocity.y /= resistance.y;
            }
        }
        resistance.set(STANDARD_RESISTANCE_X, STANDARD_RESISTANCE_Y);
        if (gravityOn) {
            velocity.add(gravity);
        }
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
        bounds.x += velocity.x * delta;
        bounds.y += velocity.y * delta;
        for (Fixture f : fixtures) {
            if (!f.attached) {
                continue;
            }
            Vector2 p = ShapeUtils.getCenterPoint(bounds).add(f.offset);
            Shape2D shape = f.shape;
            if (shape instanceof Rectangle r) {
                r.setCenter(p);
            } else if (shape instanceof Circle c) {
                c.setPosition(p);
            } else if (shape instanceof Polyline l) {
                l.setOrigin(p.x, p.y);
            }
        }
    }

    @Override
    public void reset() {
        labels.clear();
        velocity.setZero();
        Arrays.fill(senses, false);
        resistance.set(STANDARD_RESISTANCE_X, STANDARD_RESISTANCE_Y);
        for (Fixture f : fixtures) {
            Vector2 p = ShapeUtils.getCenterPoint(bounds).add(f.offset);
            Shape2D shape = f.shape;
            if (shape instanceof Rectangle r) {
                r.setCenter(p);
            } else if (shape instanceof Circle c) {
                c.setPosition(p);
            } else if (shape instanceof Polyline l) {
                l.setOrigin(p.x, p.y);
            }
        }
    }

}
