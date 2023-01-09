package com.megaman.game.entities.bosses;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.animations.Animation;
import com.megaman.game.assets.TextureAsset;
import com.megaman.game.screens.levels.Level;
import com.megaman.game.screens.menus.impl.MenuConstVals;
import com.megaman.game.utils.enums.Position;
import com.megaman.game.utils.objs.KeyValuePair;
import com.megaman.game.utils.objs.Timer;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public enum Boss {
    TIMBER_WOMAN("Timber Woman", Level.TEST5, Position.TOP_LEFT, TextureAsset.TIMBER_WOMAN) {

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(4.25f, 3.5f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("Stand", new Animation(textureAtlas.findRegion("Stand"), new float[]{1.5f, .15f}));
                put("JustLand", new Animation(textureAtlas.findRegion("JustLand"), 5, .1f, false));
                put("Swing", new Animation(textureAtlas.findRegion("Swing"), 6, .1f, false));
                put("Jump", new Animation(textureAtlas.findRegion("Jump"), 4, .1f));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("Jump"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("JustLand"), new Timer(.5f)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(1.75f)));
                add(KeyValuePair.of(anims.get("Swing"), new Timer(4f)));
            }};
        }
    },
    DISTRIBUTOR_MAN("Distributor Man", Level.TEST5, Position.TOP_CENTER, TextureAsset.DISTRIBUTOR_MAN) {

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(1.85f, 1.5f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("Stand", new Animation(textureAtlas.findRegion("Stand"), new float[]{1.5f, .15f}));
                put("Jump", new Animation(textureAtlas.findRegion("Jump")));
                put("JumpShock", new Animation(textureAtlas.findRegion("JumpShock"), 2, .15f));
                put("JustLand", new Animation(textureAtlas.findRegion("JustLand"), 2, .15f, false));
                put("Shock", new Animation(textureAtlas.findRegion("Shock"), 2, .15f));
                put("Damaged", new Animation(textureAtlas.findRegion("Damaged"), 2, .15f));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("Jump"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("JustLand"), new Timer(.3f)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(2.15f)));
                add(KeyValuePair.of(anims.get("Shock"), new Timer(1f)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(1.7f)));
            }};
        }
    },
    ROASTER_MAN("Roaster Man", Level.TEST5, Position.TOP_RIGHT, TextureAsset.ROASTER_MAN) {

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(3f, 2.5f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("Aim", new Animation(textureAtlas.findRegion("Aim")));
                put("CoolPose", new Animation(textureAtlas.findRegion("CoolPose"), 2, .3f, false));
                put("FallingWithStyle", new Animation(textureAtlas.findRegion("FallingWithStyle"), 2, .05f));
                put("FlyFlap", new Animation(textureAtlas.findRegion("FlyFlap"), 2, .2f));
                put("RetractWings", new Animation(textureAtlas.findRegion("RetractWings")));
                put("Stand", new Animation(textureAtlas.findRegion("Stand"), new float[]{1.5f, .15f}));
                put("StandFlap", new Animation(textureAtlas.findRegion("StandFlap"), 2, .2f));
                put("SuaveCombSweep", new Animation(textureAtlas.findRegion("SuaveCombSweep"), 2, .2f));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("FlyFlap"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("StandFlap"), new Timer(1.5f)));
                add(KeyValuePair.of(anims.get("RetractWings"), new Timer(.2f)));
                add(KeyValuePair.of(anims.get("SuaveCombSweep"), new Timer(.8f)));
                add(KeyValuePair.of(anims.get("CoolPose"), new Timer(4.25f)));
            }};
        }
    },
    MISTER_MAN("Mister Man", Level.TEST5, Position.CENTER_LEFT, TextureAsset.MISTER_MAN) {

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(3.25f, 2.85f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("Stand", new Animation(textureAtlas.findRegion("Stand"), new float[]{1.5f, .15f}));
                put("Jump", new Animation(textureAtlas.findRegion("Jump")));
                put("Flex", new Animation(textureAtlas.findRegion("Flex"), 2, .2f));
                put("Electrocuted", new Animation(textureAtlas.findRegion("Electrocuted"), 2, .1f));
                put("Squirt", new Animation(textureAtlas.findRegion("Squirt"), 2, .1f));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("Jump"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(1f)));
                add(KeyValuePair.of(anims.get("Flex"), new Timer(1.5f)));
                add(KeyValuePair.of(anims.get("Squirt"), new Timer(1f)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(3.25f)));
            }};
        }
    },
    BLUNT_MAN("Blunt Man", Level.TEST5, Position.CENTER_RIGHT, TextureAsset.BLUNT_MAN) {

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(1.65f, 1.5f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("Damaged", new Animation(textureAtlas.findRegion("Damaged"), 2, .1f));
                put("Flaming", new Animation(textureAtlas.findRegion("Flaming"), 2, .15f));
                put("Flex", new Animation(textureAtlas.findRegion("Flex"), 2, .2f));
                put("Jump", new Animation(textureAtlas.findRegion("Jump")));
                put("Slide", new Animation(textureAtlas.findRegion("Slide")));
                put("Stand", new Animation(textureAtlas.findRegion("Stand"), new float[]{1.5f, .15f}));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("Jump"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(1f)));
                add(KeyValuePair.of(anims.get("Flex"), new Timer(1.5f)));
                add(KeyValuePair.of(anims.get("Slide"), new Timer(.75f)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(3.5f)));
            }};
        }
    },

    // TODO: change to precious man texture asset
    PRECIOUS_MAN("Precious Man", Level.TEST5, Position.BOTTOM_LEFT, TextureAsset.PRECIOUS_MAN) {

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(2.85f, 2.5f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas atlas) {
            return new HashMap<>() {{
                put("Jump", new Animation(atlas.findRegion("Jump"), 2, .15f, false));
                put("JumpFreeze", new Animation(atlas.findRegion("JumpFreeze"), 4, .15f, false));
                put("Run", new Animation(atlas.findRegion("Jump"), 4, .15f));
                put("Stand", new Animation(atlas.findRegion("Stand"), new float[]{1.25f, .15f}));
                put("StandFreeze", new Animation(atlas.findRegion("StandFreeze"), 3, .15f));
                put("StandShoot", new Animation(atlas.findRegion("StandShoot")));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("Jump"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("StandShoot"), new Timer(.15f)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(1.6f)));
                add(KeyValuePair.of(anims.get("StandFreeze"), new Timer(2.7f)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(2.5f)));
            }};
        }
    },
    RODENT_MAN("Rodent Man", Level.RODENT_MAN, Position.BOTTOM_CENTER, TextureAsset.RODENT_MAN) {

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(2.85f, 2.5f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("Jump", new Animation(textureAtlas.findRegion("Jump"), 4, .15f));
                put("Run", new Animation(textureAtlas.findRegion("Run"), 4, .15f));
                put("Shoot", new Animation(textureAtlas.findRegion("Shoot"), 3, .15f, false));
                put("Slash", new Animation(textureAtlas.findRegion("Slash"), 2, .15f, false));
                put("Stand", new Animation(textureAtlas.findRegion("Stand"), 6, .15f));
                put("WallSlide", new Animation(textureAtlas.findRegion("WallSlide"), 2, .15f));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("Jump"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(2f)));
                add(KeyValuePair.of(anims.get("Run"), new Timer(1.2f)));
                add(KeyValuePair.of(anims.get("Slash"), new Timer(.45f)));
            }};
        }
    },
    MICROWAVE_MAN("Microwave Man", Level.TEST5, Position.BOTTOM_RIGHT, TextureAsset.MICROWAVE_MAN) {

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(2.85f, 2.5f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("HeadlessJump", new Animation(textureAtlas.findRegion("HeadlessJump")));
                put("HeadlessOpenDoor", new Animation(textureAtlas.findRegion("HeadlessOpenDoor")));
                put("HeadlessShoot", new Animation(textureAtlas.findRegion("HeadlessShoot")));
                put("HeadlessStand", new Animation(textureAtlas.findRegion("HeadlessStand")));
                put("Jump", new Animation(textureAtlas.findRegion("Jump")));
                put("OpenDoor", new Animation(textureAtlas.findRegion("OpenDoor")));
                put("Shoot", new Animation(textureAtlas.findRegion("Shoot")));
                put("Stand", new Animation(textureAtlas.findRegion("Stand"), new float[]{1.5f, .15f}));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("Jump"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(1.5f)));
                add(KeyValuePair.of(anims.get("Shoot"), new Timer(1.5f)));
                add(KeyValuePair.of(anims.get("OpenDoor"), new Timer(2.5f)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(1.25f)));
            }};
        }
    };

    public final String name;
    public final Level level;
    public final Position position;
    public final TextureAsset ass;

    public abstract Vector2 getSpriteSize();

    public abstract Map<String, Animation> getAnims(TextureAtlas textureAtlas);

    public abstract Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas);

    public static Boss findByName(String name) {
        for (Boss boss : values()) {
            if (name.equals(boss.name)) {
                return boss;
            }
        }
        return null;
    }

    public static Boss findByPos(int x, int y) {
        return findByPos(Position.getByGridIndex(x, y));
    }

    public static Boss findByPos(Position position) {
        for (Boss boss : values()) {
            if (boss.position.equals(position)) {
                return boss;
            }
        }
        return null;
    }

}
