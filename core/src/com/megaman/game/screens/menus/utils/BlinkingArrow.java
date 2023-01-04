package com.megaman.game.screens.menus.utils;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.sprites.SpriteDrawer;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldVals;
import lombok.Getter;

@Getter
public class BlinkingArrow implements Updatable, Drawable {

    private static final float ARROW_BLINK_DURATION = .2f;

    private final Sprite arrowSprite = new Sprite();
    private final Timer arrowBlinkTimer = new Timer();

    public final Vector2 center;

    private boolean arrowVisible;

    public BlinkingArrow(AssetsManager assMan, Vector2 center) {
        this.center = center;
        arrowSprite.setRegion(assMan.getTextureRegion(TextureAsset.UI_1, "Arrow"));
        arrowSprite.setSize(WorldVals.PPM / 2f, WorldVals.PPM / 2f);
        arrowSprite.setCenter(center.x, center.y);
        arrowBlinkTimer.setDuration(ARROW_BLINK_DURATION);
    }

    @Override
    public void update(float delta) {
        arrowBlinkTimer.update(delta);
        if (arrowBlinkTimer.isFinished()) {
            arrowVisible = !arrowVisible;
            arrowBlinkTimer.reset();
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (isArrowVisible()) {
            SpriteDrawer.draw(arrowSprite, batch);
        }
    }

}
