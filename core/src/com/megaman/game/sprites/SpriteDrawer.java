package com.megaman.game.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SpriteDrawer {

    public static void draw(Sprite sprite, SpriteBatch batch) {
        if (sprite.getTexture() != null) {
            sprite.draw(batch);
        }
    }

}
