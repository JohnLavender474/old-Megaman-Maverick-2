package com.megaman.game.utils;

import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.Pair;

public class ShapeUtils {

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
