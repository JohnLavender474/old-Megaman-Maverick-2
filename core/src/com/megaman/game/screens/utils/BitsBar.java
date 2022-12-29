package com.megaman.game.screens.utils;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.sprites.SpriteDrawer;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.world.WorldVals;
import lombok.Setter;

import java.util.function.Supplier;

public class BitsBar implements Drawable {

    private static final int BITS = 30;
    private static final float BIT_WIDTH = WorldVals.PPM / 2f;
    private static final float BIT_HEIGHT = WorldVals.PPM / 8f;

    private final Sprite blackBackground;
    private final Array<Sprite> bitSprites;

    @Setter
    private Supplier<Integer> countSupplier;
    @Setter
    private Supplier<Integer> maxSupplier = () -> BITS;

    public BitsBar(float x, float y, Supplier<Integer> countSupplier, AssetsManager assMan, String bitRegion) {
        this(x, y, countSupplier, assMan.getTextureRegion(TextureAsset.BITS, bitRegion),
                assMan.getTextureRegion(TextureAsset.DECORATIONS, "Black"));
    }

    public BitsBar(float x, float y, Supplier<Integer> countSupplier, Supplier<Integer> maxSupplier,
                   AssetsManager assMan, String bitRegion) {
        this(x, y, countSupplier, assMan, bitRegion);
        this.maxSupplier = maxSupplier;
    }

    public BitsBar(float x, float y, Supplier<Integer> countSupplier,
                   TextureRegion bitRegion, TextureRegion backgroundRegion) {
        this.countSupplier = countSupplier;
        this.blackBackground = new Sprite();
        this.bitSprites = new Array<>();
        Sprite bitSprite = new Sprite(bitRegion);
        bitSprite.setSize(BIT_WIDTH, BIT_HEIGHT);
        bitSprite.setX(x);
        for (int i = 0; i < BITS; i++) {
            Sprite bitSpriteCpy = new Sprite(bitSprite);
            bitSpriteCpy.setY(y + i * BIT_HEIGHT);
            bitSprites.add(bitSpriteCpy);
        }
        blackBackground.setRegion(backgroundRegion);
        blackBackground.setPosition(x, y);
    }

    public BitsBar(float x, float y, Supplier<Integer> countSupplier, Supplier<Integer> maxSupplier,
                   TextureRegion bitRegion, TextureRegion backgroundRegion) {
        this(x, y, countSupplier, bitRegion, backgroundRegion);
        this.maxSupplier = maxSupplier;
    }

    @Override
    public void draw(SpriteBatch batch) {
        blackBackground.setSize(BIT_WIDTH, maxSupplier.get() * BIT_HEIGHT);
        SpriteDrawer.draw(blackBackground, batch);
        int count = countSupplier.get();
        for (int i = 0; i < Integer.min(count, BITS); i++) {
            SpriteDrawer.draw(bitSprites.get(i), batch);
        }
    }

}
