package com.megaman.game.screens.menus.impl.bosses;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.megaman.game.animations.Animation;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.bosses.BossEnum;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.world.WorldConstVals;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
class BossPane implements Updatable, Drawable {

    public static final float PANE_BOUNDS_WIDTH = 5.33f;
    public static final float PANE_BOUNDS_HEIGHT = 4f;
    public static final float BOTTOM_OFFSET = 1.35f;
    public static final float SPRITE_HEIGHT = 2f;
    public static final float SPRITE_WIDTH = 2.5f;
    public static final float PANE_HEIGHT = 3f;
    public static final float PANE_WIDTH = 4f;

    private final String bossName;
    private final Supplier<TextureRegion> bossRegionSupplier;

    private final Sprite bossSprite = new Sprite();
    private final Sprite paneSprite = new Sprite();
    private final Animation paneBlinkingAnimation;
    private final Animation paneHighlightedAnimation;
    private final Animation paneUnhighlightedAnimation;

    private BossPaneStatus bossPaneStatus = BossPaneStatus.UNHIGHLIGHTED;

    public BossPane(AssetsManager assetsManager, BossEnum bossEnum) {
        this(assetsManager, assetsManager.getAsset(TextureAsset.BOSS_FACES.getSrc(),
                        TextureAtlas.class).findRegion(bossEnum.getBossName()),
                bossEnum.getBossName(), bossEnum.getPosition());
    }

    public BossPane(AssetsManager assetsManager, TextureRegion bossRegion, String bossName, Position position) {
        this(assetsManager, bossRegion, bossName, position.getX(), position.getY());
    }

    public BossPane(AssetsManager assetsManager, TextureRegion bossRegion, String bossName, int x, int y) {
        this(assetsManager, () -> bossRegion, bossName, x, y);
    }

    public BossPane(AssetsManager assetsManager2d, Supplier<TextureRegion> bossRegionSupplier,
                    String bossName, Position position) {
        this(assetsManager2d, bossRegionSupplier, bossName, position.getX(), position.getY());
    }

    public BossPane(AssetsManager assetsManager, Supplier<TextureRegion> bossRegionSupplier,
                    String bossName, int x, int y) {
        this.bossName = bossName;
        this.bossRegionSupplier = bossRegionSupplier;
        // center
        float centerX =
                (x * PANE_BOUNDS_WIDTH * WorldConstVals.PPM) + (PANE_BOUNDS_WIDTH * WorldConstVals.PPM / 2f);
        float centerY = (BOTTOM_OFFSET * WorldConstVals.PPM + y * PANE_BOUNDS_HEIGHT * WorldConstVals.PPM) + (PANE_BOUNDS_HEIGHT * WorldConstVals.PPM / 2f);
        // boss sprite
        bossSprite.setSize(SPRITE_WIDTH * WorldConstVals.PPM, SPRITE_HEIGHT * WorldConstVals.PPM);
        bossSprite.setCenter(centerX, centerY);
        // pane sprite
        paneSprite.setSize(PANE_WIDTH * WorldConstVals.PPM, PANE_HEIGHT * WorldConstVals.PPM);
        paneSprite.setCenter(centerX, centerY);
        // pane animations
        TextureAtlas decorationAtlas = assetsManager.getAsset(TextureAsset.STAGE_SELECT.getSrc(), TextureAtlas.class);
        TextureRegion paneUnhighlighted = decorationAtlas.findRegion("Pane");
        this.paneUnhighlightedAnimation = new Animation(paneUnhighlighted);
        TextureRegion paneBlinking = decorationAtlas.findRegion("PaneBlinking");
        this.paneBlinkingAnimation = new Animation(paneBlinking, 2, .125f);
        TextureRegion paneHighlighted = decorationAtlas.findRegion("PaneHighlighted");
        this.paneHighlightedAnimation = new Animation(paneHighlighted);
    }

    @Override
    public void update(float delta) {
        Animation timedAnimation;
        switch (bossPaneStatus) {
            case BLINKING -> timedAnimation = paneBlinkingAnimation;
            case HIGHLIGHTED -> timedAnimation = paneHighlightedAnimation;
            case UNHIGHLIGHTED -> timedAnimation = paneUnhighlightedAnimation;
            default -> throw new IllegalStateException();
        }
        timedAnimation.update(delta);
        paneSprite.setRegion(timedAnimation.getCurrentRegion());
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        Texture paneTexture = paneSprite.getTexture();
        if (paneTexture != null) {
            paneSprite.draw(spriteBatch);
        }
        TextureRegion bossTexture = bossRegionSupplier.get();
        if (bossTexture != null && bossTexture.getTexture() != null) {
            bossSprite.setRegion(bossTexture);
            bossSprite.draw(spriteBatch);
        }
    }

}
