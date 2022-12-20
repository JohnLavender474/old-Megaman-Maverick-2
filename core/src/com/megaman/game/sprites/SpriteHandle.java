package com.megaman.game.sprites;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.utils.ShapeUtils;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Drawable;

public class SpriteHandle implements Drawable, Runnable, Comparable<SpriteHandle> {

    public Sprite sprite;
    public Runnable runnable;

    public int priority;
    public boolean hidden;

    public SpriteHandle(Sprite sprite) {
        this(sprite, 0);
    }

    public SpriteHandle(Sprite sprite, int priority) {
        this(sprite, priority, null);
    }

    public SpriteHandle(Sprite sprite, int priority, Runnable runnable) {
        this.sprite = sprite;
        this.priority = priority;
        this.runnable = runnable;
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

    public void setPos(Rectangle bounds, Position pos) {
        Vector2 point = ShapeUtils.getPoint(bounds, pos);
        ShapeUtils.setToPoint(sprite.getBoundingRectangle(), point, pos);
    }

    @Override
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    @Override
    public void run() {
        if (runnable == null) {
            return;
        }
        runnable.run();
    }

    @Override
    public int compareTo(SpriteHandle o) {
        return priority - o.priority;
    }

}
