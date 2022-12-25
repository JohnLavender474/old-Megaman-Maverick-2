package com.megaman.game.movement.pendulum;

import com.badlogic.gdx.math.Vector2;
import com.megaman.game.TargetFPS;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static java.lang.Math.*;

/**
 * Resource: <a href="https://www.javacodex.com/More-Examples/2/13">...</a>
 */
@Getter
public class Pendulum implements Updatable, Resettable {

    private final float length;
    private final float gravity;
    private final Vector2 anchor;
    private final Vector2 end;

    private float angle;
    private float angleVel;
    private float angleAccel;
    private float accumulator;

    @Setter
    private float scalar = 1f;

    public Pendulum(float length, float gravity, Vector2 anchor, float scalar) {
        this(length, gravity, anchor);
        this.scalar = scalar;
    }

    public Pendulum(float length, float gravity, Vector2 anchor) {
        this.length = length;
        this.gravity = gravity;
        this.anchor = anchor;
        end = new Vector2();
        reset();
    }

    @Override
    public void update(float delta) {
        accumulator += delta;
        float tDelta = TargetFPS.getTargetDelta();
        while (accumulator >= tDelta) {
            accumulator -= tDelta;
            angleAccel = (float) (gravity / length * sin(angle));
            angleVel += angleAccel * tDelta * scalar;
            angle += angleVel * tDelta * scalar;
        }
        setEndPoint();
    }

    @Override
    public void reset() {
        angleVel = 0f;
        angleAccel = 0f;
        accumulator = 0f;
        angle = (float) PI / 2f;
        end.setZero();
    }

    private void setEndPoint() {
        end.set(getPointFromAnchor(length));
    }

    public Vector2 getPointFromAnchor(float dist) {
        Vector2 point = new Vector2();
        point.x = (float) (anchor.x + (sin(angle) * dist));
        point.y = (float) (anchor.y + (cos(angle) * dist));
        return point;
    }

}
