package com.megaman.game.pathfinding;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Entity;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Body;
import com.megaman.game.world.Fixture;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PathfindParams {

    private static final Logger logger = new Logger(PathfindParams.class, MegamanGame.DEBUG && true);

    private static final float REFRESH_TIME = .05f;

    private final Entity entity;

    final Timer refreshTimer;
    final boolean allowDiagonal;

    private final Supplier<Vector2> startSupplier;
    private final Supplier<Vector2> targetSupplier;
    private final Predicate<Fixture> rejectPredicate;
    private final Predicate<Rectangle> targetReachedPredicate;
    private final Function<Rectangle, Vector2> targetFunction;

    public PathfindParams(Entity entity, Body body,
                          Supplier<Vector2> targetSupplier,
                          Predicate<Fixture> rejectPredicate,
                          Predicate<Rectangle> targetReachedPredicate,
                          float speed, boolean allowDiagonal) {
        this.entity = entity;
        this.allowDiagonal = allowDiagonal;
        this.targetSupplier = targetSupplier;
        this.rejectPredicate = rejectPredicate;
        this.targetReachedPredicate = targetReachedPredicate;
        targetFunction = targetBounds -> {
            Vector2 start = getStart();
            Vector2 target = ShapeUtils.getCenterPoint(targetBounds);
            float angle = MathUtils.atan2(target.y - start.y, target.x - start.x);
            return new Vector2(MathUtils.cos(angle), MathUtils.sin(angle)).scl(speed);
        };
        refreshTimer = new Timer(REFRESH_TIME);
        startSupplier = () -> ShapeUtils.getCenterPoint(body.bounds);
    }

    Vector2 apply(Rectangle targetBounds) {
        return targetFunction.apply(targetBounds);
    }

    boolean reject(Array<Fixture> fixtures) {
        for (Fixture f : fixtures) {
            if (entity.equals(f.entity)) {
                continue;
            }
            if (rejectPredicate.test(f)) {
                return true;
            }
        }
        return false;
    }

    Vector2 getStart() {
        return startSupplier.get();
    }

    Vector2 getTarget() {
        return targetSupplier.get();
    }

    boolean isTargetReached(Rectangle targetBounds) {
        return targetReachedPredicate.test(targetBounds);
    }

}
