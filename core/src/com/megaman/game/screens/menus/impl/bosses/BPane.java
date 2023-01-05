package com.megaman.game.screens.menus.impl.bosses;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.bosses.BossEnum;
import com.megaman.game.sprites.SpriteDrawer;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.world.WorldVals;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
class BPane implements Updatable, Drawable {

    public static final float PANE_BOUNDS_WIDTH = 5.33f;
    public static final float PANE_BOUNDS_HEIGHT = 4f;
    public static final float BOTTOM_OFFSET = 1.35f;
    public static final float SPRITE_HEIGHT = 2f;
    public static final float SPRITE_WIDTH = 2.5f;
    public static final float PANE_HEIGHT = 3f;
    public static final float PANE_WIDTH = 4f;

    private final String bossName;
    private final Supplier<TextureRegion> bossRegSupplier;
    private final Sprite bossSprite = new Sprite();
    private final Sprite paneSprite = new Sprite();
    private final Animation paneBlinkingAnim;
    private final Animation paneHighlightedAnim;
    private final Animation paneUnhighlightedAnim;

    private BPaneStat bPaneStat = BPaneStat.UNHIGHLIGHTED;

    public BPane(MegamanGame game, BossEnum bossEnum) {
        this(game, game.getAssMan().getTextureRegion(TextureAsset.FACES_1, bossEnum.name),
                bossEnum.name, bossEnum.position);
    }

    public BPane(MegamanGame game, TextureRegion bossRegion, String bossName, Position position) {
        this(game, bossRegion, bossName, position.getX(), position.getY());
    }

    public BPane(MegamanGame game, TextureRegion bossRegion, String bossName, int x, int y) {
        this(game, () -> bossRegion, bossName, x, y);
    }

    public BPane(MegamanGame game2d, Supplier<TextureRegion> bossRegSupplier,
                 String bossName, Position position) {
        this(game2d, bossRegSupplier, bossName, position.getX(), position.getY());
    }

    public BPane(MegamanGame game, Supplier<TextureRegion> bossRegSupplier,
                 String bossName, int x, int y) {
        this.bossName = bossName;
        this.bossRegSupplier = bossRegSupplier;
        float centerX =
                (x * PANE_BOUNDS_WIDTH * WorldVals.PPM) + (PANE_BOUNDS_WIDTH * WorldVals.PPM / 2f);
        float centerY = (BOTTOM_OFFSET * WorldVals.PPM + y * PANE_BOUNDS_HEIGHT * WorldVals.PPM) +
                (PANE_BOUNDS_HEIGHT * WorldVals.PPM / 2f);
        bossSprite.setSize(SPRITE_WIDTH * WorldVals.PPM, SPRITE_HEIGHT * WorldVals.PPM);
        bossSprite.setCenter(centerX, centerY);
        paneSprite.setSize(PANE_WIDTH * WorldVals.PPM, PANE_HEIGHT * WorldVals.PPM);
        paneSprite.setCenter(centerX, centerY);
        TextureAtlas decorationAtlas = game.getAssMan().getTextureAtlas(TextureAsset.UI_1);
        TextureRegion paneUnhighlighted = decorationAtlas.findRegion("Pane");
        this.paneUnhighlightedAnim = new Animation(paneUnhighlighted);
        TextureRegion paneBlinking = decorationAtlas.findRegion("PaneBlinking");
        this.paneBlinkingAnim = new Animation(paneBlinking, 2, .125f);
        TextureRegion paneHighlighted = decorationAtlas.findRegion("PaneHighlighted");
        this.paneHighlightedAnim = new Animation(paneHighlighted);
    }

    @Override
    public void update(float delta) {
        Animation timedAnimation;
        switch (bPaneStat) {
            case BLINKING -> timedAnimation = paneBlinkingAnim;
            case HIGHLIGHTED -> timedAnimation = paneHighlightedAnim;
            case UNHIGHLIGHTED -> timedAnimation = paneUnhighlightedAnim;
            default -> throw new IllegalStateException();
        }
        timedAnimation.update(delta);
        paneSprite.setRegion(timedAnimation.getCurrRegion());
    }

    @Override
    public void draw(SpriteBatch batch) {
        SpriteDrawer.draw(paneSprite, batch);
        bossSprite.setRegion(bossRegSupplier.get());
        SpriteDrawer.draw(bossSprite, batch);
    }

}
