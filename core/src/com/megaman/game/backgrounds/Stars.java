package com.megaman.game.backgrounds;

import com.megaman.game.MegamanGame;
import com.megaman.game.ViewVals;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.world.WorldVals;

public class Stars extends Background {

    private static final int ROWS = 1;
    private static final int COLS = 6;
    private static final float DUR = 10f;
    private static final float WIDTH = ViewVals.VIEW_WIDTH / 3f;
    private static final float HEIGHT = ViewVals.VIEW_HEIGHT / 4f;

    private float dist;

    public Stars(MegamanGame game, float x, float y) {
        super(game.getAssMan().getTextureRegion(TextureAsset.BACKGROUNDS_1, "StarFieldBG"),
                x, y, WIDTH, HEIGHT, ROWS, COLS);
    }

    @Override
    public void update(float delta) {
        float trans = WIDTH * WorldVals.PPM * delta / DUR;
        translate(-trans, 0f);
        dist += trans;
        if (dist >= WIDTH * WorldVals.PPM) {
            resetPositions();
            dist = 0f;
        }
    }

}
