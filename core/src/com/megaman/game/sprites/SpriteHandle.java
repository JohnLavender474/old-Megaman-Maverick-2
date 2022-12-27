package com.megaman.game.sprites;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.interfaces.Positional;
import com.megaman.game.utils.interfaces.Updatable;

public class SpriteHandle implements Drawable, Positional, Updatable, Comparable<SpriteHandle> {

    public Sprite sprite;
    public Updatable updatable;

    public int priority;
    public boolean hidden;

    public SpriteHandle(Sprite sprite) {
        this(sprite, 0);
    }

    public SpriteHandle(Sprite sprite, int priority) {
        this(sprite, priority, null);
    }

    public SpriteHandle(Sprite sprite, int priority, Updatable updatable) {
        this.sprite = sprite;
        this.priority = priority;
        this.updatable = updatable;
    }

    public SpriteHandle(TextureRegion region) {
        this(region, 0f, 0f);
    }

    public SpriteHandle(TextureRegion region, float w, float h) {
        this(region, 0f, 0f, w, h);
    }

    public SpriteHandle(TextureRegion region, float x, float y, float w, float h) {
        sprite = new Sprite(region);
        sprite.setBounds(x, y, w, h);
    }

    public boolean isInCamBounds(Camera cam) {
        return cam.frustum.boundsInFrustum(ShapeUtils.rectToBBox(sprite.getBoundingRectangle()));
    }

    public void setPosition(Rectangle bounds, Position pos) {
        Vector2 p = ShapeUtils.getPoint(bounds, pos);
        setPosition(p, pos);
    }

    public void setPosition(Vector2 p, Position pos) {
        switch (pos) {
            case BOTTOM_LEFT -> sprite.setPosition(p.x, p.y);
            case BOTTOM_CENTER -> sprite.setPosition(p.x - sprite.getWidth() / 2f, p.y);
            case BOTTOM_RIGHT -> sprite.setPosition(p.x - sprite.getWidth(), p.y);
            case CENTER_LEFT -> {
                sprite.setCenter(p.x, p.y);
                sprite.setX(sprite.getX() + sprite.getWidth() / 2f);
            }
            case CENTER -> sprite.setCenter(p.x, p.y);
            case CENTER_RIGHT -> {
                sprite.setCenter(p.x, p.y);
                sprite.setX(sprite.getX() - sprite.getWidth() / 2f);
            }
            case TOP_LEFT -> sprite.setPosition(p.x, p.y - sprite.getHeight());
            case TOP_CENTER -> {
                sprite.setCenter(p.x, p.y);
                sprite.setY(sprite.getY() - sprite.getHeight() / 2f);
            }
            case TOP_RIGHT -> sprite.setPosition(p.x - sprite.getWidth(), p.y - sprite.getHeight());
        }
    }

    public void setPosition(Vector2 p, Position pos, float xOffset, float yOffset) {
        setPosition(p, pos);
        sprite.translate(xOffset, yOffset);
    }

    public void setPosition(Rectangle bounds, Position pos, float xOffset, float yOffset) {
        setPosition(bounds, pos);
        sprite.translate(xOffset, yOffset);
    }

    @Override
    public void draw(SpriteBatch batch) {
        SpriteDrawer.draw(sprite, batch);
    }

    @Override
    public void update(float delta) {
        if (updatable == null) {
            return;
        }
        updatable.update(delta);
    }

    @Override
    public int compareTo(SpriteHandle o) {
        return priority - o.priority;
    }

    @Override
    public Vector2 getPosition() {
        return new Vector2(sprite.getX(), sprite.getY());
    }

    @Override
    public void setPosition(float x, float y) {
        sprite.setPosition(x, y);
    }

}
