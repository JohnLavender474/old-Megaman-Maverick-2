package com.megaman.game.screens.menus.impl.bosses;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.Animation;
import com.megaman.game.assets.MusicAsset;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.bosses.BossEnum;
import com.megaman.game.screens.menus.MenuButton;
import com.megaman.game.screens.menus.MenuScreen;
import com.megaman.game.screens.menus.utils.BlinkingArrow;
import com.megaman.game.screens.menus.utils.ScreenSlide;
import com.megaman.game.ui.TextHandle;
import com.megaman.game.utils.ConstFuncs;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.TimeMarkedRunnable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.ViewVals;
import com.megaman.game.world.WorldConstVals;

import java.util.*;
import java.util.function.Supplier;

import static com.megaman.game.world.WorldConstVals.PPM;

public class BossSelectScreen extends MenuScreen {

    private static final Vector3 INTRO_BLOCKS_TRANS = new Vector3(15f, 0f, 0f).scl(WorldConstVals.PPM);
    private static final Set<String> bossNames = new HashSet<>() {{
        for (BossEnum bossEnum : BossEnum.values()) {
            add(bossEnum.bossName);
        }
    }};
    private static final String MEGA_MAN = "MEGA MAN";
    private static final String BACK = "BACK";

    private final Sound bloopSound;
    private final TextHandle bossName;
    private final ScreenSlide screenSlide;

    private final Sprite blackBar1 = new Sprite();
    private final Sprite blackBar2 = new Sprite();

    private final List<Sprite> backgroundSprites = new ArrayList<>();
    private final List<TextHandle> texts = new ArrayList<>();
    private final List<BossPane> bossPanes = new ArrayList<>();

    private final Map<Sprite, Animation> bars = new HashMap<>();
    private final Map<String, BlinkingArrow> blinkingArrows = new HashMap<>();

    private boolean outro;
    private boolean blink;
    private BossEnum bossEnumSelection;

    private final Timer outroTimer = new Timer(1.05f, new ArrayList<>() {{
        for (int i = 1; i <= 10; i++) {
            add(new TimeMarkedRunnable(.1f * i, () -> blink = !blink));
        }
    }});
    private final Sprite whiteSprite = new Sprite();

    public BossSelectScreen(MegamanGame megamanGame) {
        super(megamanGame, MusicAsset.STAGE_SELECT_MM3_MUSIC.getSrc(), MEGA_MAN);
        this.screenSlide = new ScreenSlide(uiCam, INTRO_BLOCKS_TRANS,
                ConstFuncs.getCamInitPos().sub(INTRO_BLOCKS_TRANS), ConstFuncs.getCamInitPos(), .5f);
        // sound
        this.bloopSound = assetsManager.getAsset(SoundAsset.CURSOR_MOVE_BLOOP_SOUND.getSrc(), Sound.class);
        // Megaman faces
        TextureAtlas megamanFacesAtlas = assetsManager.getAsset(
                TextureAsset.MEGAMAN_FACES.getSrc(), TextureAtlas.class);
        Map<Position, TextureRegion> megamanFaces = new EnumMap<>(Position.class);
        for (Position position : Position.values()) {
            TextureRegion faceRegion = megamanFacesAtlas.findRegion(position.name());
            megamanFaces.put(position, faceRegion);
        }
        Supplier<TextureRegion> megamanFaceSupplier = () -> {
            BossEnum bossEnum = BossEnum.findByName(getCurrBtnKey());
            if (bossEnum == null) {
                return megamanFaces.get(Position.CENTER);
            }
            return megamanFaces.get(bossEnum.getPosition());
        };
        BossPane megamanPane = new BossPane(assetsManager, megamanFaceSupplier, MEGA_MAN, Position.CENTER);
        bossPanes.add(megamanPane);
        // boss bossPanes
        for (BossEnum boss : BossEnum.values()) {
            BossPane bossPane = new BossPane(assetsManager, boss);
            bossPanes.add(bossPane);
        }
        // text and blinking arrows
        texts.add(new TextHandle(new Vector2(5.35f * PPM, 13.85f * PPM), () -> "PRESS START"));
        texts.add(new TextHandle(new Vector2(12.35f * PPM, PPM), () -> BACK));
        blinkingArrows.put(BACK, new BlinkingArrow(assetsManager, new Vector2(12f * PPM, .75f * PPM)));
        // backgMath.round bars
        TextureAtlas stageSelectAtlas = assetsManager.getAsset(TextureAsset.STAGE_SELECT.getSrc(), TextureAtlas.class);
        TextureRegion bar = stageSelectAtlas.findRegion("Bar");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                Sprite sprite = new Sprite(bar);
                sprite.setBounds(i * 3f * PPM, (j * 4f * PPM) + 1.35f * PPM, 5.33f * PPM, 4f * PPM);
                Animation timedAnimation = new Animation(bar, new float[]{.3f, .15f, .15f, .15f});
                bars.put(sprite, timedAnimation);
            }
        }
        // white sprite and black bar
        TextureAtlas decorationsAtlas = assetsManager.getAsset(TextureAsset.DECORATIONS.getSrc(), TextureAtlas.class);
        TextureRegion white = decorationsAtlas.findRegion("White");
        whiteSprite.setRegion(white);
        whiteSprite.setBounds(0f, 0f, ViewVals.VIEW_WIDTH * PPM, ViewVals.VIEW_HEIGHT * PPM);
        TextureRegion black = decorationsAtlas.findRegion("Black");
        blackBar1.setRegion(black);
        blackBar1.setBounds(-PPM, -PPM, (2f + ViewVals.VIEW_WIDTH) * PPM, 2f * PPM);
        blackBar2.setRegion(black);
        blackBar2.setBounds(0f, 0f, .25f * PPM, ViewVals.VIEW_HEIGHT * PPM);
        // backgMath.round block sprites
        TextureAtlas tilesAtlas = assetsManager.getAsset(TextureAsset.CUSTOM_TILES_1.getSrc(), TextureAtlas.class);
        TextureRegion blueBlockRegion = tilesAtlas.findRegion("8bitBlueBlockTransBorder");
        final float halfPPM = PPM / 2f;
        for (int i = 0; i < ViewVals.VIEW_WIDTH; i++) {
            for (int j = 0; j < ViewVals.VIEW_HEIGHT - 1; j++) {
                for (int x = 0; x < 2; x++) {
                    for (int y = 0; y < 2; y++) {
                        Sprite blueBlock = new Sprite(blueBlockRegion);
                        blueBlock.setBounds(i * PPM + (x * halfPPM), j * PPM + (y * halfPPM), halfPPM, halfPPM);
                        backgroundSprites.add(blueBlock);
                    }
                }
            }
        }
        // boss name
        bossName = new TextHandle(new Vector2(PPM, PPM), () -> "");
    }

    @Override
    public void show() {
        super.show();
        screenSlide.init();
        outroTimer.reset();
        outro = false;
    }

    @Override
    protected void onAnyMovement() {
        Sound bloopSound = assetsManager.getAsset(SoundAsset.CURSOR_MOVE_BLOOP_SOUND.getSrc(), Sound.class);
        // TODO: Set volume
        bloopSound.play();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        // slide screen if intro
        screenSlide.update(delta);
        // begin spritebatch

        batch.begin();
        // render white sprite on blink if outro
        if (outro) {
            outroTimer.update(delta);
            if (blink) {
                whiteSprite.draw(batch);
            }
        }
        // render backgMath.round blocks
        backgroundSprites.forEach(s -> s.draw(batch));
        // render flashy bar sprites
        bars.forEach((sprite, animation) -> {
            animation.update(delta);
            sprite.setRegion(animation.getCurrentRegion());
            sprite.draw(batch);
        });
        // render boss panes
        bossPanes.forEach(bossPane -> {
            if (bossPane.getBossName().equals(getCurrBtnKey())) {
                bossPane.setBossPaneStatus(isSelectionMade() ? BossPaneStatus.HIGHLIGHTED : BossPaneStatus.BLINKING);
            } else {
                bossPane.setBossPaneStatus(BossPaneStatus.UNHIGHLIGHTED);
            }
            bossPane.update(delta);
            bossPane.draw(batch);
        });
        // render black bars
        blackBar1.draw(batch);
        blackBar2.draw(batch);
        // render blinking arrow
        if (blinkingArrows.containsKey(getCurrBtnKey())) {
            BlinkingArrow blinkingArrow = blinkingArrows.get(getCurrBtnKey());
            blinkingArrow.update(delta);
            blinkingArrow.draw(batch);
        }
        // render text
        texts.forEach(text -> text.draw(batch));
        // render boss name
        if (MEGA_MAN.equals(getCurrBtnKey()) || bossNames.contains(getCurrBtnKey())) {
            bossName.setText(getCurrBtnKey().toUpperCase());
            bossName.draw(batch);
        }
        // end spritebatch
        batch.end();
        // if outro is finished, set screen to level selection
        if (outroTimer.isFinished()) {
            // ((LevelIntroScreen) assMan.getScreen(LEVEL_INTRO)).set(bossEnumSelection);
            // TODO: Set screen
            // assMan.setScreen(ScreenEnum.LEVEL_INTRO);
            // TODO: Set screen
        }
    }

    @Override
    protected ObjectMap<String, MenuButton> defineMenuButtons() {
        ObjectMap<String, MenuButton> menuButtons = new ObjectMap<>();
        menuButtons.put(MEGA_MAN, new MenuButton() {

            @Override
            public boolean onSelect(float delta) {
                return false;
            }

            @Override
            public void onNavigate(Direction direction, float delta) {
                switch (direction) {
                    case DIR_UP -> setMenuButton(BossEnum.findByPos(1, 2).getBossName());
                    case DIR_DOWN -> setMenuButton(BossEnum.findByPos(1, 0).getBossName());
                    case DIR_LEFT -> setMenuButton(BossEnum.findByPos(0, 1).getBossName());
                    case DIR_RIGHT -> setMenuButton(BossEnum.findByPos(2, 1).getBossName());
                }
            }

        });
        menuButtons.put(BACK, new MenuButton() {

            @Override
            public boolean onSelect(float delta) {
                // assMan.setScreen(MAIN_MENU);
                // TODO: Set screen
                return true;
            }

            @Override
            public void onNavigate(Direction direction, float delta) {
                switch (direction) {
                    case DIR_UP, DIR_LEFT, DIR_RIGHT -> setMenuButton(BossEnum.findByPos(2, 0).getBossName());
                    case DIR_DOWN -> setMenuButton(BossEnum.findByPos(2, 2).getBossName());
                }
            }

        });
        for (BossEnum bossEnum : BossEnum.values()) {
            menuButtons.put(bossEnum.getBossName(), new MenuButton() {

                @Override
                public boolean onSelect(float delta) {
                    assetsManager.getAsset(SoundAsset.BEAM_OUT_SOUND.getSrc(), Sound.class).play();
                    bossEnumSelection = bossEnum;
                    outro = true;
                    music.stop();
                    return true;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    int x = bossEnum.getPosition().getX();
                    int y = bossEnum.getPosition().getY();
                    switch (direction) {
                        case DIR_UP -> y += 1;
                        case DIR_DOWN -> y -= 1;
                        case DIR_LEFT -> x -= 1;
                        case DIR_RIGHT -> x += 1;
                    }
                    if (y < 0 || y > 2) {
                        setMenuButton(BACK);
                        return;
                    }
                    if (x < 0) {
                        x = 2;
                    }
                    if (x > 2) {
                        x = 0;
                    }
                    Position position = Position.getByGridIndex(x, y);
                    if (position == null) {
                        throw new IllegalStateException();
                    } else if (position.equals(Position.CENTER)) {
                        setMenuButton(MEGA_MAN);
                    } else {
                        setMenuButton(BossEnum.findByPos(x, y).getBossName());
                    }
                }

            });
        }
        return menuButtons;
    }

}
