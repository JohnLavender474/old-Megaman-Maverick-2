package com.megaman.game.screens.other;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.megaman.game.MegamanGame;
import com.megaman.game.ViewVals;
import com.megaman.game.animations.Animation;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.backgrounds.Stars;
import com.megaman.game.screens.menus.impl.bosses.BEnum;
import com.megaman.game.screens.utils.TextHandle;
import com.megaman.game.utils.objs.KeyValuePair;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldVals;

import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Supplier;

public class BIntroScreen extends ScreenAdapter {

    private static final float DUR = 7f;
    private static final float B_DROP = .25f;
    private static final float B_LET_DELAY = 1f;
    private static final float B_LET_DUR = .2f;
    private static final int STARS_N_BARS = 4;

    private static Map<BEnum, Supplier<Queue<KeyValuePair<Animation, Timer>>>> bIntroAnims;

    private final MegamanGame game;

    private final Timer bLettersDelay;
    private final Timer bLettersTimer;
    private final Animation barAnim;
    private final Array<Sprite> bars;
    private final Array<Stars> stars;
    private final Timer durTimer;
    private final Timer bDropTimer;
    private final TextHandle bName;

    private BEnum b;
    private Queue<Runnable> bLetterAnimQ;
    private KeyValuePair<Sprite, Queue<KeyValuePair<Animation, Timer>>> currBAnim;

    public BIntroScreen(MegamanGame game) {
        this.game = game;
        bLettersDelay = new Timer(B_LET_DELAY);
        bLettersTimer = new Timer(B_LET_DUR);
        TextureRegion barReg = game.getAssMan().getTextureRegion(TextureAsset.STAGE_SELECT, "Bar");
        barAnim = new Animation(barReg, new float[]{.3f, .15f, .15f, .15f});
        bars = new Array<>() {{
            for (int i = 0; i < STARS_N_BARS; i++) {
                Sprite bar = new Sprite();
                bar.setBounds(
                        (i * ViewVals.VIEW_WIDTH * WorldVals.PPM / 3f) - 5f,
                        ViewVals.VIEW_HEIGHT * WorldVals.PPM / 3f,
                        (ViewVals.VIEW_WIDTH * WorldVals.PPM / 3f) + 5f,
                        ViewVals.VIEW_HEIGHT * WorldVals.PPM / 3f);
                add(bar);
            }
        }};
        stars = new Array<>() {{
            for (int i = 0; i < STARS_N_BARS; i++) {
                add(new Stars(
                        game,
                        0f,
                        i * WorldVals.PPM * ViewVals.VIEW_HEIGHT / 4f,
                        i + 1));
            }
        }};
        durTimer = new Timer(DUR);
        bDropTimer = new Timer(B_DROP);
        bName = new TextHandle(new Vector2(
                (ViewVals.VIEW_WIDTH * WorldVals.PPM / 3f) - WorldVals.PPM,
                ViewVals.VIEW_HEIGHT * WorldVals.PPM / 3f));
        if (bIntroAnims == null) {
            bIntroAnims = new EnumMap<>(BEnum.class);
            for (BEnum b : BEnum.values()) {
                bIntroAnims.put(b, () -> b.getIntroAnimsQ(game.getAssMan().getTextureAtlas(b.ass)));
            }
        }
    }

    public void set(BEnum b) {
        this.b = b;
        Sprite s = new Sprite();
        Vector2 size = b.getSpriteSize();
        s.setSize(size.x * WorldVals.PPM, size.y * WorldVals.PPM);
        currBAnim = new KeyValuePair<>(s, bIntroAnims.get(b).get());
        bLetterAnimQ.clear();
        for (int i = 0; i < b.name.length(); i++) {
            final int finalI = i;
            bLetterAnimQ.add(() -> {
                bName.setText(b.name.substring(0, finalI + 1));
                if (Character.isWhitespace(b.name.charAt(finalI))) {
                    return;
                }
                game.getAudioMan().playSound(SoundAsset.THUMP_SOUND);
            });
        }
    }

}
