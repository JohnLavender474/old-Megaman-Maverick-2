package com.megaman.game.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.utils.enums.Direction;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UtilMethods {

    private static final Logger logger = new Logger(UtilMethods.class, MegamanGame.DEBUG && false);
    private static final Random RAND = new Random(System.currentTimeMillis());

    public static int getRandom(int min, int max) {
        return RAND.nextInt(max + 1 - min) + min;
    }

    public static void doIfRandMatch(int min, int max, Iterable<Integer> matches, Consumer<Integer> runOnMatch) {
        int r = getRandom(min, max);
        logger.log("Random r: " + r);
        for (Integer i : matches) {
            if (r == i) {
                logger.log("Run on match!");
                runOnMatch.accept(r);
                break;
            }
        }
    }

    public static String toString(ObjectMap<?, ?> m) {
        StringBuilder sb = new StringBuilder("{");
        for (ObjectMap.Entry<?, ?> e : m.entries()) {
            sb.append("[").append(e.key).append(" : ").append(e.value).append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    public static <T> boolean mask(T o1, T o2, Predicate<T> p1, Predicate<T> p2) {
        return (p1.test(o1) && p2.test(o2)) || (p2.test(o1) && p1.test(o2));
    }

    public static <T extends Enum<T>> boolean isNameOfEnum(Class<T> enumClass, String name) {
        try {
            Enum.valueOf(enumClass, name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static Direction getSingleMostDirectionFromStartToTarget(Rectangle start, Rectangle target) {
        Vector2 startCenter = ShapeUtils.getCenterPoint(start);
        Vector2 targetCenter = ShapeUtils.getCenterPoint(target);
        return UtilMethods.getSingleMostDirectionFromStartToTarget(startCenter, targetCenter);
    }

    public static Direction getSingleMostDirectionFromStartToTarget(Vector2 start, Vector2 target) {
        float x = target.x - start.x;
        float y = target.y - start.y;
        if (Math.abs(x) > Math.abs(y)) {
            return x > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return y > 0 ? Direction.UP : Direction.DOWN;
        }
    }

    public static float getSlope(Vector2 p1, Vector2 p2) {
        return (p1.y - p2.y) / (p1.x - p2.x);
    }

    public static boolean isInCamBounds(Camera camera, Rectangle rectangle) {
        return camera.frustum.boundsInFrustum(rectToBBox(rectangle));
    }

    public static <T extends Number & Comparable<T>> T clampNumber(T number, T min, T max) {
        if (number.compareTo(min) < 0) {
            number = min;
        } else if (number.compareTo(max) > 0) {
            number = max;
        }
        return number;
    }

    public static boolean equalsAny(Object test, Object... objs) {
        for (Object obj : objs) {
            if (test.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    public static Vector2 normalizedTrajectory(Vector2 start, Vector2 end, float speed) {
        float x = end.x - start.x;
        float y = end.y - start.y;
        float length = (float) Math.sqrt(x * x + y * y);
        x /= length;
        y /= length;
        return new Vector2(x * speed, y * speed);
    }

    public static float roundedFloat(float num, int decimals) {
        float scale = (float) Math.pow(10, decimals);
        return Math.round(num * scale) / scale;
    }

    public static void roundedVector2(Vector2 vector2, int decimals) {
        vector2.x = UtilMethods.roundedFloat(vector2.x, decimals);
        vector2.y = UtilMethods.roundedFloat(vector2.y, decimals);
    }

    public static BoundingBox rectToBBox(Rectangle rectangle) {
        return new BoundingBox(new Vector3(rectangle.getX(), rectangle.getY(), 0.0f),
                new Vector3(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight(), 0.0f));
    }

    public static Vector3 toVec3(Vector2 vector2) {
        return new Vector3(vector2.x, vector2.y, 0f);
    }

    public static Vector2 toVec2(Vector3 vector3) {
        return new Vector2(vector3.x, vector3.y);
    }

    public static String objName(Object o) {
        return o.getClass().getSimpleName();
    }

    public static Vector2 interpolate(Vector2 start, Vector2 target, float delta) {
        Vector2 interPos = new Vector2();
        interPos.x = UtilMethods.interpolate(start.x, target.x, delta);
        interPos.y = UtilMethods.interpolate(start.y, target.y, delta);
        return interPos;
    }

    public static float interpolate(float start, float target, float delta) {
        return start - (start - target) * delta;
    }

    public static Direction getOverlapPushDirection(Rectangle toBePushed, Rectangle other, Rectangle overlap) {
        Intersector.intersectRectangles(toBePushed, other, overlap);
        if (overlap.width == 0f && overlap.height == 0f) {
            return null;
        }
        if (overlap.width > overlap.height) {
            return toBePushed.y > other.y ? Direction.UP : Direction.DOWN;
        } else {
            return toBePushed.x > other.x ? Direction.RIGHT : Direction.LEFT;
        }
    }

}
