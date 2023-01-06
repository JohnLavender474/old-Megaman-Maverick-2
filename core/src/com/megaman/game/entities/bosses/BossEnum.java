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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public enum BossEnum {
    TIMBER_WOMAN("Timber Woman", Level.TEST5, Position.TOP_LEFT, TextureAsset.TIMBER_WOMAN) {
        @Override
        public String getBio() {
            return "Originally named \"Timbre Woman\", she was to be the best singer " +
                    "in the world. However, one of the programmers messed up and put " +
                    "\"Timber\" instead of \"Timbre\" in her code. So as a result, she " +
                    "became the world's strongest lumberjack instead.";
        }

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
        public String getBio() {
            return "Distributor Man is adept at energized distribution solutions, " +
                    "or so says his LinkedIn bio anyways. He lives in the current, " +
                    "dwelling not on the past or the future, calling this " +
                    "philosophy \"living in constant happy shock!\".";
        }

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
        public String getBio() {
            return "Roaster Man is a world-renowned chicken chef. He believes strongly " +
                    "that happy chickens make for the best cuisines. He dedicates his " +
                    "time to engaging chickens, making them happy so that when they die, " +
                    "they die in bliss and warmth. \"Their warm feelings of happiness,\" " +
                    "he says, \"makes for the best roast!\"";
        }

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
        public String getBio() {
            return "Mister Man waters delicate plants for a living and believes " +
                    "strongly in having purpose in life. His favorite motto " +
                    "for motivation is \"Don't say it. Spray it.\"";
        }

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
        public String getBio() {
            return "As an endorser of cannabis, Blunt Man is not afraid " +
                    "of being blunt about his opinions. He is also a savvy " +
                    "business man, having many joint ventures. He loves " +
                    "taking many profound trips around the galaxy.";
        }

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
    NUKE_MAN("Nuke Man", Level.TEST5, Position.BOTTOM_LEFT, TextureAsset.NUKE_MAN) {
        @Override
        public String getBio() {
            return "Designed as a nuclear arms expert, Nuke Man is " +
                    "able to build a nuclear bomb out of common " +
                    "household items. Deemed too dangerous to be " +
                    "kept alive, he is now a fugitive on the run. " +
                    "He has vowed revenge on the world and must be " +
                    "stopped at once! There's few things he hates " +
                    "more than hippies, pacifists, and pot smokers.";
        }

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(2.85f, 2.5f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("Attack", new Animation(textureAtlas.findRegion("Attack")));
                put("BendKnees", new Animation(textureAtlas.findRegion("BendKnees")));
                put("Charge", new Animation(textureAtlas.findRegion("Charge"), 2, .15f));
                put("Jump", new Animation(textureAtlas.findRegion("Jump")));
                put("Stand", new Animation(textureAtlas.findRegion("Stand"), new float[]{1.5f, .15f}));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("Jump"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(1f)));
                add(KeyValuePair.of(anims.get("Charge"), new Timer(1.25f)));
                add(KeyValuePair.of(anims.get("Attack"), new Timer(.25f)));
                add(KeyValuePair.of(anims.get("BendKnees"), new Timer(.75f)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(3.5f)));
            }};
        }
    },
    FREEZER_MAN("Freezer Man", Level.FREEZER_MAN, Position.BOTTOM_CENTER, TextureAsset.FREEZER_MAN) {
        @Override
        public String getBio() {
            return "Freezer man is good at freezing things. So much that he is " +
                    "commissioned by the scientific community to refreeze the melting " +
                    "ice caps in the arctic regions. He enjoys hot tea and believes leftover " +
                    "spaghetti is superior to when it's fresh.";
        }

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(3f, 2.5f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("GiveTheHand", new Animation(textureAtlas.findRegion("GiveTheHand")));
                put("Jump", new Animation(textureAtlas.findRegion("Jump")));
                put("JumpOpenFreezer", new Animation(textureAtlas.findRegion("JumpOpenFreezer")));
                put("Opening", new Animation(textureAtlas.findRegion("Opening"), 3, .15f, false));
                put("StandLookDown", new Animation(textureAtlas.findRegion("StandLookDown"),
                        new float[]{1.5f, .15f}));
                put("StandLookUp", new Animation(textureAtlas.findRegion("StandLookUp"),
                        new float[]{1.5f, .15f}));
                put("StandOpenFridge", new Animation(textureAtlas.findRegion("StandOpenFridge")));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("Jump"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("StandLookUp"), new Timer(.75f)));
                add(KeyValuePair.of(anims.get("StandLookDown"), new Timer(.75f)));
                add(KeyValuePair.of(anims.get("GiveTheHand"), new Timer(.25f)));
                add(KeyValuePair.of(anims.get("Opening"), new Timer(.5f)));
                add(KeyValuePair.of(anims.get("StandOpenFridge"), new Timer(5f)));
            }};
        }
    },
    MICROWAVE_MAN("Microwave Man", Level.TEST5, Position.BOTTOM_RIGHT, TextureAsset.MICROWAVE_MAN) {
        @Override
        public String getBio() {
            return "Microwave Man is a microwave. It's a fate that has lead him to question " +
                    "his life. Although at first he was severely depressed about being a " +
                    "microwave, he soon accepted it as a fact of life and now tours " +
                    "giving motivational speeches.";
        }

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

    public abstract String getBio();

    public abstract Vector2 getSpriteSize();

    public abstract Map<String, Animation> getAnims(TextureAtlas textureAtlas);

    public abstract Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas);

    public static BossEnum findByName(String name) {
        for (BossEnum boss : values()) {
            if (name.equals(boss.name)) {
                return boss;
            }
        }
        return null;
    }

    public static BossEnum findByPos(int x, int y) {
        return findByPos(Position.getByGridIndex(x, y));
    }

    public static BossEnum findByPos(Position position) {
        for (BossEnum boss : values()) {
            if (boss.position.equals(position)) {
                return boss;
            }
        }
        return null;
    }

}
