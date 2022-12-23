package com.megaman.game.shapes;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShapeUtils {

    public static boolean intersectLineRect(Polyline polyline, Rectangle rectangle, Collection<Vector2> interPoints) {
        float[] v = polyline.getTransformedVertices();
        return intersectLineRect(v, rectangle, interPoints);
    }

    public static boolean intersectLineRect(float[] v, Rectangle rectangle, Collection<Vector2> interPoints) {
        Pair<Vector2> line = Pair.of(new Vector2(v[0], v[1]), new Vector2(v[2], v[3]));
        return intersectLineRect(line, rectangle, interPoints);
    }

    public static boolean intersectLineRect(Pair<Vector2> line, Rectangle rectangle, Collection<Vector2> interPoints) {
        List<Pair<Vector2>> rectToLines = rectToLines(rectangle);
        boolean isIntersection = false;
        for (Pair<Vector2> l : rectToLines) {
            Vector2 intersection = new Vector2();
            if (intersectLines(l, line, intersection)) {
                isIntersection = true;
                interPoints.add(intersection);
            }
        }
        return isIntersection;
    }

    public static List<Pair<Vector2>> rectToLines(Rectangle rect) {
        List<Pair<Vector2>> lines = new ArrayList<>();
        lines.add(Pair.of(getTopLeftPoint(rect), getTopRightPoint(rect)));
        lines.add(Pair.of(new Vector2(rect.x, rect.y), getBottomRightPoint(rect)));
        lines.add(Pair.of(new Vector2(rect.x, rect.y), getTopLeftPoint(rect)));
        lines.add(Pair.of(getBottomRightPoint(rect), getTopRightPoint(rect)));
        return lines;
    }

    public static Rectangle getBoundingRect(Shape2D s) {
        if (s instanceof Rectangle r) {
            return r;
        } else if (s instanceof Circle c) {
            return new Rectangle().setSize(c.radius * 2f).setCenter(c.x, c.y);
        } else if (s instanceof Polyline p) {
            return p.getBoundingRectangle();
        }
        throw new IllegalStateException("Unsupported Shape2D class: " + s.getClass());
    }

    public static boolean overlaps(Shape2D s1, Shape2D s2) {
        Pair<Shape2D> p = new Pair<>();
        if (maskShapeTypes(s1, s2, Rectangle.class)) {
            return Intersector.overlaps((Rectangle) s1, (Rectangle) s2);
        } else if (maskShapeTypes(s1, s2, Circle.class)) {
            return Intersector.overlaps((Circle) s1, (Circle) s2);
        } else if (maskShapeTypes(s1, s2, Polyline.class)) {
            return overlapLines((Polyline) s1, (Polyline) s2);
        } else if (maskShapeTypes(s1, s2, Rectangle.class, Circle.class, p)) {
            return Intersector.overlaps((Circle) p.getSecond(), (Rectangle) p.getFirst());
        } else if (maskShapeTypes(s1, s2, Rectangle.class, Polyline.class, p)) {
            Polyline polyline = (Polyline) p.getSecond();
            float[] v = polyline.getTransformedVertices();
            return Intersector.intersectSegmentRectangle(
                    new Vector2(v[0], v[1]),
                    new Vector2(v[2], v[3]),
                    (Rectangle) p.getFirst());
        } else if (maskShapeTypes(s1, s2, Circle.class, Polyline.class, p)) {
            Pair<Vector2> line = polylineToPointPair((Polyline) p.getSecond());
            return Intersector.intersectSegmentCircle(
                    line.getFirst(),
                    line.getSecond(),
                    (Circle) p.getFirst(),
                    null);
        }
        return false;
    }

    public static boolean maskShapeTypes(Shape2D s1, Shape2D s2,
                                         Class<? extends Shape2D> c1,
                                         Class<? extends Shape2D> c2,
                                         Pair<Shape2D> p) {
        if (c1.isAssignableFrom(s1.getClass()) && c2.isAssignableFrom(s2.getClass())) {
            p.set(s1, s2);
            return true;
        } else if (c1.isAssignableFrom(s2.getClass()) && c2.isAssignableFrom(s1.getClass())) {
            p.set(s2, s1);
            return true;
        }
        return false;
    }

    public static boolean maskShapeTypes(Shape2D s1, Shape2D s2, Class<? extends Shape2D> c) {
        return c.isAssignableFrom(s1.getClass()) && c.isAssignableFrom(s2.getClass());
    }

    public static boolean overlapLines(Polyline line1, Polyline line2) {
        return intersectLines(line1, line2, new Vector2());
    }

    public static boolean intersectLines(Polyline line1, Polyline line2, Vector2 intersection) {
        return intersectLines(polylineToPointPair(line1), polylineToPointPair(line2), intersection);
    }

    public static boolean intersectLines(Pair<Vector2> l1, Pair<Vector2> l2, Vector2 intersection) {
        return Intersector.intersectSegments(
                l1.getFirst(), l1.getSecond(), l2.getFirst(), l2.getSecond(), intersection);
    }

    public static Pair<Vector2> polylineToPointPair(Polyline polyline) {
        float[] v = polyline.getTransformedVertices();
        return polylineToPointPair(v);
    }

    public static Pair<Vector2> polylineToPointPair(float[] v) {
        Vector2 p1 = new Vector2(v[0], v[1]);
        Vector2 p2 = new Vector2(v[2], v[3]);
        return Pair.of(p1, p2);
    }

    public static BoundingBox rectToBBox(Rectangle rectangle) {
        return new BoundingBox(new Vector3(rectangle.x, rectangle.y, 0f),
                new Vector3(rectangle.x + rectangle.width, rectangle.y + rectangle.height, 0f));
    }

    public static Vector2 getPoint(Rectangle rect, Position pos) {
        return switch (pos) {
            case BOTTOM_RIGHT -> getBottomRightPoint(rect);
            case BOTTOM_CENTER -> getBottomCenterPoint(rect);
            case BOTTOM_LEFT -> getBottomLeftPoint(rect);
            case CENTER_RIGHT -> getCenterRightPoint(rect);
            case CENTER -> getCenterPoint(rect);
            case CENTER_LEFT -> getCenterLeftPoint(rect);
            case TOP_RIGHT -> getTopRightPoint(rect);
            case TOP_CENTER -> getTopCenterPoint(rect);
            case TOP_LEFT -> getTopLeftPoint(rect);
        };
    }

    public static void setToPoint(Rectangle rect, Vector2 point, Position pos) {
        switch (pos) {
            case BOTTOM_RIGHT -> setBottomRightToPoint(rect, point);
            case BOTTOM_CENTER -> setBottomCenterToPoint(rect, point);
            case BOTTOM_LEFT -> setBottomLeftToPoint(rect, point);
            case CENTER_RIGHT -> setCenterRightToPoint(rect, point);
            case CENTER -> setCenterToPoint(rect, point);
            case CENTER_LEFT -> setCenterLeftToPoint(rect, point);
            case TOP_RIGHT -> setTopRightToPoint(rect, point);
            case TOP_CENTER -> setTopCenterToPoint(rect, point);
            case TOP_LEFT -> setTopLeftToPoint(rect, point);
        }
    }

    public static Vector2 getBottomRightPoint(Rectangle rect) {
        return new Vector2(rect.x + rect.width, rect.y);
    }

    public static void setBottomRightToPoint(Rectangle rect, Vector2 bottomRightPoint) {
        rect.setPosition(bottomRightPoint.x - rect.width, bottomRightPoint.y);
    }

    public static Vector2 getBottomCenterPoint(Rectangle rect) {
        return new Vector2(rect.x + rect.width / 2f, rect.y);
    }

    public static void setBottomCenterToPoint(Rectangle rect, Vector2 bottomCenterPoint) {
        rect.setPosition(bottomCenterPoint.x - rect.width / 2f, bottomCenterPoint.y);
    }

    public static Vector2 getBottomLeftPoint(Rectangle rect) {
        return new Vector2(rect.x, rect.y);
    }

    public static void setBottomLeftToPoint(Rectangle rect, Vector2 bottomLeftPoint) {
        rect.setPosition(bottomLeftPoint);
    }

    public static Vector2 getCenterRightPoint(Rectangle rect) {
        return new Vector2(rect.x + rect.width, rect.y + (rect.height / 2f));
    }

    public static void setCenterRightToPoint(Rectangle rect, Vector2 centerRightPoint) {
        rect.setPosition(centerRightPoint.x - rect.width, centerRightPoint.y - (rect.height / 2f));
    }

    public static Vector2 getCenterPoint(Rectangle rect) {
        Vector2 center = new Vector2();
        rect.getCenter(center);
        return center;
    }

    public static void setCenterToPoint(Rectangle rect, Vector2 centerPoint) {
        rect.setCenter(centerPoint);
    }

    public static Vector2 getCenterLeftPoint(Rectangle rect) {
        return new Vector2(rect.x, rect.y + (rect.height / 2f));
    }

    public static void setCenterLeftToPoint(Rectangle rect, Vector2 centerLeftPoint) {
        rect.setPosition(centerLeftPoint.x, centerLeftPoint.y - (rect.height / 2f));
    }

    public static Vector2 getTopRightPoint(Rectangle rect) {
        return new Vector2(rect.x + rect.width, rect.y + rect.height);
    }

    public static void setTopRightToPoint(Rectangle rect, Vector2 topRightPoint) {
        rect.setPosition(topRightPoint.x - rect.width, topRightPoint.y - rect.height);
    }

    public static Vector2 getTopCenterPoint(Rectangle rect) {
        return new Vector2(rect.x + (rect.width / 2f), rect.y + rect.height);
    }

    public static void setTopCenterToPoint(Rectangle rect, Vector2 topCenterPoint) {
        rect.setPosition(topCenterPoint.x - (rect.width / 2f), topCenterPoint.y - rect.height);
    }

    public static Vector2 getTopLeftPoint(Rectangle rect) {
        return new Vector2(rect.x, rect.y + rect.height);
    }

    public static void setTopLeftToPoint(Rectangle rect, Vector2 topLeftPoint) {
        rect.setPosition(topLeftPoint.x, topLeftPoint.y - rect.height);
    }

}
