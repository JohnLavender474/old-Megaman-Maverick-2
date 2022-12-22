package com.megaman.game.screens.ui;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.world.WorldVals;

import java.util.function.Supplier;

public class BitsBar implements Drawable {

    private static final int BITS = 30;

    private final Sprite blackBackground;
    private final Array<Sprite> bitSprites;
    private final Supplier<Integer> countSupplier;

    public BitsBar(float x, float y, Supplier<Integer> countSupplier, AssetsManager assMan, String bitRegion) {
        this(x, y, countSupplier, assMan.getTextureRegion(TextureAsset.BITS, bitRegion),
                assMan.getTextureRegion(TextureAsset.DECORATIONS, "Black"));
    }

    public BitsBar(float x, float y, Supplier<Integer> countSupplier,
                   TextureRegion bitRegion, TextureRegion backgroundRegion) {
        this.countSupplier = countSupplier;
        this.blackBackground = new Sprite();
        this.bitSprites = new Array<>();
        float bitWidth = WorldVals.PPM / 2f;
        float bitHeight = WorldVals.PPM / 8f;
        Sprite bitSprite = new Sprite(bitRegion);
        bitSprite.setSize(bitWidth, bitHeight);
        bitSprite.setX(x);
        for (int i = 0; i < BITS; i++) {
            Sprite bitSpriteCpy = new Sprite(bitSprite);
            bitSpriteCpy.setY(y + i * bitHeight);
            bitSprites.add(bitSpriteCpy);
        }
        blackBackground.setRegion(backgroundRegion);
        blackBackground.setBounds(x, y, bitWidth, bitHeight * BITS);
    }

    @Override
    public void draw(SpriteBatch batch) {
        blackBackground.draw(batch);
        int count = countSupplier.get();
        for (int i = 0; i < Integer.min(count, BITS); i++) {
            bitSprites.get(i).draw(batch);
        }
    }

}
