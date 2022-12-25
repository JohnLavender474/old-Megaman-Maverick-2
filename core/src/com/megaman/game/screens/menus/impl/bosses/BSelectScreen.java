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
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.screens.ScreenEnum;
import com.megaman.game.screens.menus.MenuButton;
import com.megaman.game.screens.menus.MenuScreen;
import com.megaman.game.screens.menus.utils.BlinkingArrow;
import com.megaman.game.screens.menus.utils.ScreenSlide;
import com.megaman.game.screens.utils.TextHandle;
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
    private BEnum bSelect;

    public BSelectScreen(MegamanGame game) {
        super(game, MEGA_MAN);
        if (bNameSet == null) {
            bNameSet = new ObjectSet<>();
            for (BEnum b : BEnum.values()) {
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
        TextureAtlas megamanFacesAtlas = assMan.getTextureAtlas(TextureAsset.MEGAMAN_FACES);
        Map<Position, TextureRegion> megamanFaces = new EnumMap<>(Position.class);
        for (Position position : Position.values()) {
            TextureRegion faceRegion = megamanFacesAtlas.findRegion(position.name());
            megamanFaces.put(position, faceRegion);
        }
        Supplier<TextureRegion> megamanFaceSupplier = () -> {
            BEnum bEnum = BEnum.findByName(getCurrBtnKey());
            if (bEnum == null) {
                return megamanFaces.get(Position.CENTER);
            }
            return megamanFaces.get(bEnum.position);
        };
        BPane megamanPane = new BPane(game, megamanFaceSupplier, MEGA_MAN, Position.CENTER);
        bp.add(megamanPane);
        for (BEnum boss : BEnum.values()) {
            BPane bPane = new BPane(game, boss);
            bp.add(bPane);
        }
        t.add(new TextHandle(new Vector2(5.35f * WorldVals.PPM, 13.85f * WorldVals.PPM), "PRESS START"));
        t.add(new TextHandle(new Vector2(12.35f * WorldVals.PPM, WorldVals.PPM), BACK));
        bArrs.put(BACK, new BlinkingArrow(assMan, new Vector2(12f * WorldVals.PPM, .75f * WorldVals.PPM)));
        TextureAtlas stageSelectAtlas = assMan.getTextureAtlas(TextureAsset.STAGE_SELECT);
        TextureRegion bar = stageSelectAtlas.findRegion("Bar");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                Sprite sprite = new Sprite(bar);
                sprite.setBounds(i * 3f * WorldVals.PPM, (j * 4f * WorldVals.PPM) + 1.35f * WorldVals.PPM, 
                        5.33f * WorldVals.PPM, 4f * WorldVals.PPM);
                Animation timedAnimation = new Animation(bar, new float[]{.3f, .15f, .15f, .15f});
                bars.put(sprite, timedAnimation);
            }
        }
        TextureAtlas decorationsAtlas = assMan.getTextureAtlas(TextureAsset.DECORATIONS);
        TextureRegion whiteReg = decorationsAtlas.findRegion("White");
        white.setRegion(whiteReg);
        white.setBounds(0f, 0f, ViewVals.VIEW_WIDTH * WorldVals.PPM, ViewVals.VIEW_HEIGHT * WorldVals.PPM);
        TextureRegion black = decorationsAtlas.findRegion("Black");
        bar1.setRegion(black);
        bar1.setBounds(-WorldVals.PPM, -WorldVals.PPM,
                (2f + ViewVals.VIEW_WIDTH) * WorldVals.PPM, 2f * WorldVals.PPM);
        bar2.setRegion(black);
        bar2.setBounds(0f, 0f, .25f * WorldVals.PPM, ViewVals.VIEW_HEIGHT * WorldVals.PPM);
        TextureAtlas tilesAtlas = assMan.getTextureAtlas(TextureAsset.CUSTOM_TILES_1);
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
        outTimer.reset();
        outro = false;
    }

    @Override
    protected void onAnyMovement() {
        audioMan.playSound(SoundAsset.CURSOR_MOVE_BLOOP_SOUND);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        slide.update(delta);
        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        if (outro) {
            outTimer.update(delta);
            if (blink) {
                white.draw(batch);
            }
        }
        for (Sprite b : bkgd) {
            b.draw(batch);
        }
        for (ObjectMap.Entry<Sprite, Animation> e : bars) {
            e.value.update(delta);
            e.key.setRegion(e.value.getCurrRegion());
            e.key.draw(batch);
        }
        for (BPane b : bp) {
            if (b.getBossName().equals(getCurrBtnKey())) {
                b.setBPaneStat(isSelectionMade() ? BPaneStat.HIGHLIGHTED : BPaneStat.BLINKING);
            } else {
                b.setBPaneStat(BPaneStat.UNHIGHLIGHTED);
            }
            b.update(delta);
            b.draw(batch);
        }
        bar1.draw(batch);
        bar2.draw(batch);
        if (bArrs.containsKey(getCurrBtnKey())) {
            BlinkingArrow blinkingArrow = bArrs.get(getCurrBtnKey());
            blinkingArrow.update(delta);
            blinkingArrow.draw(batch);
        }
        t.forEach(text -> text.draw(batch));
        if (MEGA_MAN.equals(getCurrBtnKey()) || bNameSet.contains(getCurrBtnKey())) {
            bName.setText(getCurrBtnKey().toUpperCase());
            bName.draw(batch);
        }
        batch.end();
        if (outTimer.isFinished()) {
            
            // TODO: set level intro screen
            /*
            ((LevelIntroScreen) game.getScreen(LEVEL_INTRO)).set(bSelect);
            game.setScreen(LEVEL_INTRO);            
             */
            
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
                    case UP -> setMenuButton(BEnum.findByPos(1, 2).name);
                    case DOWN -> setMenuButton(BEnum.findByPos(1, 0).name);
                    case LEFT -> setMenuButton(BEnum.findByPos(0, 1).name);
                    case RIGHT -> setMenuButton(BEnum.findByPos(2, 1).name);
                }
            }
        });
        menuButtons.put(BACK, new MenuButton() {
            @Override
            public boolean onSelect(float delta) {
                game.setScreen(ScreenEnum.MAIN_MENU);
                return true;
            }

            @Override
            public void onNavigate(Direction direction, float delta) {
                switch (direction) {
                    case UP, LEFT, RIGHT -> setMenuButton(BEnum.findByPos(2, 0).name);
                    case DOWN -> setMenuButton(BEnum.findByPos(2, 2).name);
                }
            }
        });
        for (BEnum bEnum : BEnum.values()) {
            menuButtons.put(bEnum.name, new MenuButton() {
                @Override
                public boolean onSelect(float delta) {
                    assMan.getSound(SoundAsset.BEAM_OUT_SOUND);
                    bSelect = bEnum;
                    outro = true;
                    music.stop();
                    return true;
                }

                @Override
                public void onNavigate(Direction direction, float delta) {
                    int x = bEnum.position.getX();
                    int y = bEnum.position.getY();
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
                        setMenuButton(BEnum.findByPos(x, y).name);
                    }
                }
            });
        }
        return menuButtons;
    }

}
