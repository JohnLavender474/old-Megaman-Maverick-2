package com.megaman.game.screens.menus.impl.bosses;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.megaman.game.MegamanGame;
import com.megaman.game.ViewVals;
import com.megaman.game.animations.Animation;
import com.megaman.game.assets.MusicAsset;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.entities.impl.bosses.BossType;
import com.megaman.game.screens.ScreenEnum;
import com.megaman.game.screens.menus.MenuButton;
import com.megaman.game.screens.menus.MenuScreen;
import com.megaman.game.screens.menus.utils.BlinkingArrow;
import com.megaman.game.screens.menus.utils.ScreenSlide;
import com.megaman.game.screens.other.BIntroScreen;
import com.megaman.game.screens.utils.TextHandle;
import com.megaman.game.sprites.SpriteDrawer;
import com.megaman.game.utils.ConstFuncs;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.TimeMarkedRunnable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldVals;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class BSelectScreen extends MenuScreen {

    private static final Vector3 INTRO_BLOCKS_TRANS = new Vector3(15f * WorldVals.PPM, 0f, 0f);
    private static final String MEGA_MAN = "MEGA MAN";
    private static final String BACK = "BACK";

    private static ObjectSet<String> bNameSet;

    private final TextHandle bName;
    private final ScreenSlide slide;
    private final Sprite bar1;
    private final Sprite bar2;
    private final Sprite white;
    private final Timer outTimer;
    private final Array<TextHandle> t;
    private final Array<BPane> bp;
    private final Array<Sprite> bkgd;
    private final ObjectMap<Sprite, Animation> bars;
    private final ObjectMap<String, BlinkingArrow> bArrs;

    private boolean outro;
    private boolean blink;
    private BossType bSelect;

    public BSelectScreen(MegamanGame game) {
        super(game, MEGA_MAN);
        if (bNameSet == null) {
            bNameSet = new ObjectSet<>();
            for (BossType b : BossType.values()) {
                bNameSet.add(b.name);
            }
        }
        bar1 = new Sprite();
        bar2 = new Sprite();
        white = new Sprite();
        t = new Array<>();
        bp = new Array<>();
        bkgd = new Array<>();
        bars = new ObjectMap<>();
        bArrs = new ObjectMap<>();
        outTimer = new Timer(1.05f, new Array<>() {{
            for (int i = 1; i <= 10; i++) {
                add(new TimeMarkedRunnable(.1f * i, () -> blink = !blink));
            }
        }});
        slide = new ScreenSlide(uiCam, INTRO_BLOCKS_TRANS,
                ConstFuncs.getCamInitPos().sub(INTRO_BLOCKS_TRANS), ConstFuncs.getCamInitPos(), .5f);
        TextureAtlas megamanFacesAtlas = assMan.getTextureAtlas(TextureAsset.FACES_1);
        Map<Position, TextureRegion> megamanFaces = new EnumMap<>(Position.class);
        for (Position position : Position.values()) {

            // TODO: Maverick instead of Megaman faces?
            TextureRegion faceRegion = megamanFacesAtlas.findRegion("Maverick/" + position.name());
            megamanFaces.put(position, faceRegion);

        }
        Supplier<TextureRegion> megamanFaceSupplier = () -> {
            BossType boss = BossType.findByName(getCurrBtnKey());
            if (boss == null) {
                return megamanFaces.get(Position.CENTER);
            }
            return megamanFaces.get(boss.position);
        };
        BPane megamanPane = new BPane(game, megamanFaceSupplier, MEGA_MAN, Position.CENTER);
        bp.add(megamanPane);
        for (BossType boss : BossType.values()) {
            bp.add(new BPane(game, boss));
        }
        t.add(new TextHandle(new Vector2(5.35f * WorldVals.PPM, 13.85f * WorldVals.PPM), "PRESS START"));
        t.add(new TextHandle(new Vector2(12.35f * WorldVals.PPM, WorldVals.PPM), BACK));
        bArrs.put(BACK, new BlinkingArrow(assMan, new Vector2(12f * WorldVals.PPM, .75f * WorldVals.PPM)));
        TextureAtlas stageSelectAtlas = assMan.getTextureAtlas(TextureAsset.UI_1);
        TextureRegion bar = stageSelectAtlas.findRegion("Bar");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                Sprite sprite = new Sprite(bar);
                sprite.setBounds(
                        i * 3f * WorldVals.PPM,
                        (j * 4f * WorldVals.PPM) + 1.35f * WorldVals.PPM,
                        5.33f * WorldVals.PPM,
                        4f * WorldVals.PPM);
                Animation timedAnimation = new Animation(bar, new float[]{.3f, .15f, .15f, .15f});
                bars.put(sprite, timedAnimation);
            }
        }
        TextureAtlas colorsAtlas = assMan.getTextureAtlas(TextureAsset.COLORS);
        TextureRegion whiteReg = colorsAtlas.findRegion("White");
        white.setRegion(whiteReg);
        white.setBounds(0f, 0f, ViewVals.VIEW_WIDTH * WorldVals.PPM, ViewVals.VIEW_HEIGHT * WorldVals.PPM);
        TextureRegion black = colorsAtlas.findRegion("Black");
        bar1.setRegion(black);
        bar1.setBounds(-WorldVals.PPM, -WorldVals.PPM,
                (2f + ViewVals.VIEW_WIDTH) * WorldVals.PPM, 2f * WorldVals.PPM);
        bar2.setRegion(black);
        bar2.setBounds(0f, 0f, .25f * WorldVals.PPM, ViewVals.VIEW_HEIGHT * WorldVals.PPM);
        TextureAtlas tilesAtlas = assMan.getTextureAtlas(TextureAsset.PLATFORMS_1);
        TextureRegion blueBlockRegion = tilesAtlas.findRegion("8bitBlueBlockTransBorder");
        final float halfPPM = WorldVals.PPM / 2f;
        for (int i = 0; i < ViewVals.VIEW_WIDTH; i++) {
            for (int j = 0; j < ViewVals.VIEW_HEIGHT - 1; j++) {
                for (int x = 0; x < 2; x++) {
                    for (int y = 0; y < 2; y++) {
                        Sprite blueBlock = new Sprite(blueBlockRegion);
                        blueBlock.setBounds(i * WorldVals.PPM + (x * halfPPM), j * WorldVals.PPM + (y * halfPPM),
                                halfPPM, halfPPM);
                        bkgd.add(blueBlock);
                    }
                }
            }
        }
        bName = new TextHandle(new Vector2(WorldVals.PPM, WorldVals.PPM));
    }

    @Override
    public void show() {
        super.show();
        slide.init();
        outro = false;
        outTimer.reset();
        game.getAudioMan().playMusic(MusicAsset.STAGE_SELECT_MM3_MUSIC, true);
    }

    @Override
    protected void onAnyMovement() {
        audioMan.playMusic(SoundAsset.CURSOR_MOVE_BLOOP_SOUND);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (!game.isPaused()) {
            slide.update(delta);
            if (outro) {
                outTimer.update(delta);
            }
            if (outTimer.isFinished()) {
                game.setScreen(ScreenEnum.BOSS_INTRO, BIntroScreen.class, s -> s.set(bSelect));
                return;
            }
            for (ObjectMap.Entry<Sprite, Animation> e : bars) {
                e.value.update(delta);
                e.key.setRegion(e.value.getCurrRegion());
            }
            for (BPane b : bp) {
                if (b.getBossName().equals(getCurrBtnKey())) {
                    b.setBPaneStat(isSelectionMade() ? BPaneStat.HIGHLIGHTED : BPaneStat.BLINKING);
                } else {
                    b.setBPaneStat(BPaneStat.UNHIGHLIGHTED);
                }
                b.update(delta);
            }
            if (bArrs.containsKey(getCurrBtnKey())) {
                bArrs.get(getCurrBtnKey()).update(delta);
            }
        }
        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        if (outro && blink) {
            SpriteDrawer.draw(white, batch);
        }
        for (Sprite b : bkgd) {
            SpriteDrawer.draw(b, batch);
        }
        for (ObjectMap.Entry<Sprite, Animation> e : bars) {
            SpriteDrawer.draw(e.key, batch);
        }
        for (BPane b : bp) {
            b.draw(batch);
        }
        SpriteDrawer.draw(bar1, batch);
        SpriteDrawer.draw(bar2, batch);
        if (bArrs.containsKey(getCurrBtnKey())) {
            bArrs.get(getCurrBtnKey()).draw(batch);
        }
        for (TextHandle text : t) {
            text.draw(batch);
        }
        if (MEGA_MAN.equals(getCurrBtnKey()) || bNameSet.contains(getCurrBtnKey())) {
            bName.setText(getCurrBtnKey().toUpperCase());
            bName.draw(batch);
        }
        batch.end();
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
                    case UP -> setMenuButton(BossType.findByPos(1, 2).name);
                    case DOWN -> setMenuButton(BossType.findByPos(1, 0).name);
                    case LEFT -> setMenuButton(BossType.findByPos(0, 1).name);
                    case RIGHT -> setMenuButton(BossType.findByPos(2, 1).name);
                }
            }
        });
        menuButtons.put(BACK, new MenuButton() {
            @Override
            public boolean onSelect(float delta) {
                game.setScreen(game.getScreen(ScreenEnum.MAIN));
                return true;
            }

            @Override
            public void onNavigate(Direction direction, float delta) {
                switch (direction) {
                    case UP, LEFT, RIGHT -> setMenuButton(BossType.findByPos(2, 0).name);
                    case DOWN -> setMenuButton(BossType.findByPos(2, 2).name);
                }
            }
        });
        for (BossType boss : BossType.values()) {
            menuButtons.put(boss.name, new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    audioMan.playMusic(SoundAsset.BEAM_OUT_SOUND);
                    audioMan.stopMusic();
                    bSelect = boss;
                    outro = true;
                    return true;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    int x = boss.position.getX();
                    int y = boss.position.getY();
                    switch (direction) {
                        case UP -> y += 1;
                        case DOWN -> y -= 1;
                        case LEFT -> x -= 1;
                        case RIGHT -> x += 1;
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
                        setMenuButton(BossType.findByPos(x, y).name);
                    }
                }
            });
        }
        return menuButtons;
    }

}
