package com.megaman.game.ui;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.world.WorldConstVals;

import java.util.function.Supplier;

public class MainBitsBarUi implements Drawable {

    private static final int BITS = 30;

    private final Supplier<Integer> countSupplier;
    private final Sprite blackBackground = new Sprite();
    private final Array<Sprite> bitSprites = new Array<>(BITS);

    public MainBitsBarUi(Supplier<Integer> countSupplier, TextureRegion bitRegion, TextureRegion backgroundRegion) {
        this.countSupplier = countSupplier;
        float x = WorldConstVals.PPM * .4f;
        float y = WorldConstVals.PPM * 9f;
        float bitWidth = WorldConstVals.PPM / 2f;
        float bitHeight = WorldConstVals.PPM / 8f;
        Sprite bitSprite = new Sprite(bitRegion);
        bitSprite.setSize(bitWidth, bitHeight);
        bitSprite.setX(x);
        for (int i = 0; i < BITS; i++) {
            Sprite bitSpriteCpy = new Sprite(bitSprite);
            bitSpriteCpy.setY(y + i * bitHeight);
            bitSprites.add(bitSprite);
        }
        blackBackground.setRegion(backgroundRegion);
        blackBackground.setBounds(x, y, bitWidth, bitHeight * BITS);
    }

    @Override
    public void draw(SpriteBatch batch) {
        blackBackground.draw(batch);
        int count = countSupplier.get();
        for (int i = 0; i < Integer.min(30, count); i++) {
            bitSprites.get(i).draw(batch);
        }
    }

}
