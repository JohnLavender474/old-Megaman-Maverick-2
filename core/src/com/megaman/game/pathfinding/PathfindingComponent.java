package com.megaman.game.pathfinding;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.Component;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Body;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PathfindingComponent implements Component {

    public static final float DEFAULT_REFRESH_TIME = .5f;

    public LinkedList<Vector2> currPath;

    public Body body;
    public float speed;
    public Timer refreshTimer;

    public Predicate<Object> reject;
    public Supplier<Vector2> startSupplier;
    public Supplier<Vector2> targetSupplier;
    public Consumer<Vector2> targetConsumer;
    public Predicate<Vector2> pointReachedPred;

    public boolean persistOldPath = true;
    public boolean allowDiagonal = true;

    public PathfindingComponent(Body body, Supplier<Vector2> targetSupplier, float speed) {
        this(body, targetSupplier, obj -> false, speed, DEFAULT_REFRESH_TIME);
    }

    public PathfindingComponent(Body body, Supplier<Vector2> targetSupplier, Predicate<Object> reject,
                                float speed, float timeToRefresh) {
        this.body = body;
        this.startSupplier = () -> ShapeUtils.getCenterPoint(this.body.bounds);
        this.targetSupplier = targetSupplier;
        this.speed = speed;
        this.targetConsumer = target -> {
            Vector2 start = getStart();
            float angle = MathUtils.atan2(target.y - start.y, target.x - start.x);
            this.body.velocity.set(MathUtils.cos(angle), MathUtils.sin(angle)).scl(this.speed);
        };
        this.reject = reject;
        this.refreshTimer = new Timer(timeToRefresh);
        this.pointReachedPred = body.bounds::contains;
    }

    public void consume(Vector2 p) {
        targetConsumer.accept(p);
    }

    public boolean isReject(Object o) {
        return reject.test(o);
    }

    public boolean isReject(Iterable<Object> objs) {
        for (Object o : objs) {
            if (isReject(o)) {
                return true;
            }
        }
        return false;
    }

    public Vector2 getStart() {
        return startSupplier.get();
    }

    public boolean isPointReached(Vector2 p) {
        return pointReachedPred.test(p);
    }

    public Vector2 getTarget() {
        return targetSupplier.get();
    }

}
