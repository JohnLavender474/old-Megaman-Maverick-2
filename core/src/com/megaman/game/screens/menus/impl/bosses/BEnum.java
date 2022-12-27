package com.megaman.game.screens.menus.impl.bosses;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.animations.Animation;
import com.megaman.game.assets.MusicAsset;
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
public enum BEnum {
    TIMBER_WOMAN("Timber Woman", Level.TEST5, MusicAsset.XENOBLADE_GAUR_PLAINS_MUSIC,
            Position.TOP_LEFT, TextureAsset.TIMBER_WOMAN) {
        @Override
        public String getBio() {
            return "Originally named 'Timbre Woman', she was to be \n" +
                    "the prettiest singer in the world. But one of the \n" +
                    "senior programmers messed up and accidentally typed \n" +
                    "'Timber' instead of 'Timbre', and as a result she \n" +
                    "became the world's strongest lumberjack. She keeps \n" +
                    "water off her axe adamantly and has beheaded over \n" +
                    "a thousand chickens in her lifetime.";
        }

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(3f, 2.5f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("Stand", new Animation(textureAtlas.findRegion("Stand"), new float[]{1.5f, .15f}));
                put("JustLand", new Animation(textureAtlas.findRegion("JustLand"), 6, .1f, false));
                put("AboutToSwing", new Animation(textureAtlas.findRegion("AboutToSwing"), 3, .15f, false));
                put("Swing", new Animation(textureAtlas.findRegion("Swing"), 4, .15f, false));
                put("Jump", new Animation(textureAtlas.findRegion("Jump"), 6, MenuConstVals.BOSS_DROP_DOWN / 12));
            }};
        }

        @Override
        public Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas) {
            Map<String, Animation> anims = getAnims(textureAtlas);
            return new LinkedList<>() {{
                add(KeyValuePair.of(anims.get("Jump"), new Timer(MenuConstVals.BOSS_DROP_DOWN)));
                add(KeyValuePair.of(anims.get("JustLand"), new Timer(.6f)));
                add(KeyValuePair.of(anims.get("Stand"), new Timer(1.75f)));
                add(KeyValuePair.of(anims.get("AboutToSwing"), new Timer(.375f)));
                add(KeyValuePair.of(anims.get("Swing"), new Timer(4f)));
            }};
        }
    },
    DISTRIBUTOR_MAN("Distributor Man", Level.TEST5, MusicAsset.XENOBLADE_GAUR_PLAINS_MUSIC,
            Position.TOP_CENTER, TextureAsset.DISTRIBUTOR_MAN) {
        @Override
        public String getBio() {
            return "Distributor Man designs electrical components \n" +
                    "that require advanced timed signals. His \n" +
                    "favorite hobby is using his distribution \n" +
                    "power to spread awareness about the energetic \n" +
                    "benefits of good exercise and a healthy diet. \n" +
                    "He is best friends with Blunt Man even though \n" +
                    "too much of Blunt's hazy demeanor sometimes \n" +
                    "disorientates his calibrations.";
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
    ROASTER_MAN("Roaster Man", Level.TEST5, MusicAsset.XENOBLADE_GAUR_PLAINS_MUSIC,
            Position.TOP_RIGHT, TextureAsset.ROASTER_MAN) {
        @Override
        public String getBio() {
            return "Roaster Man was designed as a chicken chef \n" +
                    "but has since renounced meat-eating, preferring \n" +
                    "instead to cook corn cuisines. He believes \n" +
                    "microwaves are a blight on culinary arts. He \n" +
                    "is an ardent Hindu and is described as being \n" +
                    "scrupulous and brooding. He greatly enjoys \n" +
                    "pecking things apart, getting to the kernel \n" +
                    "of things, and roasting others in debates.";
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
    MISTER_MAN("Mister Man", Level.TEST5, MusicAsset.XENOBLADE_GAUR_PLAINS_MUSIC,
            Position.CENTER_LEFT, TextureAsset.MISTER_MAN) {
        @Override
        public String getBio() {
            return "Pump Man's brother, Mister Man waters delicate \n" +
                    "plants for a living. He loves misty mornings, \n" +
                    "fine scents, and isn't afraid to pull the trigger \n" +
                    "on spontaneous ideas. He believes that having a \n" +
                    "spray-and-pray attitude is the best thing one can \n" +
                    "do to handle the chaos and disappointments in life. \n" +
                    "Oh, and he is also very afraid of electricity.";
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
    BLUNT_MAN("Blunt Man", Level.TEST5, MusicAsset.XENOBLADE_GAUR_PLAINS_MUSIC,
            Position.CENTER_RIGHT, TextureAsset.BLUNT_MAN) {
        @Override
        public String getBio() {
            return "Blunt Man is designed to be an activist for the \n" +
                    "legalization of cannabis and recreational drugs. \n" +
                    "He likes to take things easy but isn't afraid of \n" +
                    "being blunt about his opinions. He has a joint \n" +
                    "venture in a recreational drug research company \n" +
                    "and spends his fortunes on profound trips. He not \n" +
                    "a fan of getting nuked.One criticism of him is he \n" +
                    "lobbies against independent distributors.";
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
    NUKE_MAN("Nuke Man", Level.TEST5, MusicAsset.XENOBLADE_GAUR_PLAINS_MUSIC,
            Position.BOTTOM_LEFT, TextureAsset.NUKE_MAN) {
        @Override
        public String getBio() {
            return "Designed as a nuclear arms expert, Nuke Man is \n" +
                    "able to build a nuclear bomb out of common\n" +
                    "household items. Deemed too dangerous to be \n" +
                    "kept alive, he is now a fugitive on the run. \n" +
                    "He has vowed revenge on the world and must be \n" +
                    "stopped at once! There's few things he hates \n" +
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
    FRIDGE_MAN("Fridge Man", Level.TEST5, MusicAsset.XENOBLADE_GAUR_PLAINS_MUSIC,
            Position.BOTTOM_CENTER, TextureAsset.FRIDGE_MAN) {
        @Override
        public String getBio() {
            return "Fridge Man really enjoys putting leftovers inside \n" +
                    "himself. He specializes in following you around and \n" +
                    "storing your leftovers. He is made of a very strong \n" +
                    "metal that is resistant to nuclear blasts, a fact \n" +
                    "he loves to boast about. There's nothing he hates \n" +
                    "more than microwaving because it implies taking \n" +
                    "food out of him which leaves him feeling empty.";
        }

        @Override
        public Vector2 getSpriteSize() {
            return new Vector2(4.5f, 4f);
        }

        @Override
        public Map<String, Animation> getAnims(TextureAtlas textureAtlas) {
            return new HashMap<>() {{
                put("GiveTheHand", new Animation(textureAtlas.findRegion("GiveTheHand")));
                put("Jump", new Animation(textureAtlas.findRegion("Jump")));
                put("JumpOpenFreezer", new Animation(textureAtlas.findRegion("JumpOpenFreezer")));
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
                add(KeyValuePair.of(anims.get("StandLookUp"), new Timer(1.25f)));
                add(KeyValuePair.of(anims.get("StandLookDown"), new Timer(1f)));
                add(KeyValuePair.of(anims.get("GiveTheHand"), new Timer(.75f)));
                add(KeyValuePair.of(anims.get("StandOpenFridge"), new Timer(3.75f)));
            }};
        }
    },
    MICROWAVE_MAN("Microwave Man", Level.TEST5, MusicAsset.XENOBLADE_GAUR_PLAINS_MUSIC,
            Position.BOTTOM_RIGHT, TextureAsset.MICROWAVE_MAN) {
        @Override
        public String getBio() {
            return "Microwave Man is a microwave. It's a fate that \n" +
                    "has lead him to question his life. Although at \n" +
                    "first he was severely depressed about being a \n" +
                    "microwave, he soon accepted it as a fact of life \n" +
                    "and nowadays tours giving motivational speeches. \n" +
                    "Although he's skilled at microwaving, he's too \n" +
                    "small for things like whole chickens to fit into.";
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
    public final MusicAsset music;
    public final Position position;
    public final TextureAsset ass;

    public abstract String getBio();

    public abstract Vector2 getSpriteSize();

    public abstract Map<String, Animation> getAnims(TextureAtlas textureAtlas);

    public abstract Queue<KeyValuePair<Animation, Timer>> getIntroAnimsQ(TextureAtlas textureAtlas);

    public static BEnum findByName(String name) {
        for (BEnum boss : values()) {
            if (name.equals(boss.name)) {
                return boss;
            }
        }
        return null;
    }

    public static BEnum findByPos(int x, int y) {
        return findByPos(Position.getByGridIndex(x, y));
    }

    public static BEnum findByPos(Position position) {
        for (BEnum boss : values()) {
            if (boss.position.equals(position)) {
                return boss;
            }
        }
        return null;
    }

}
