package com.megaman.game.screens.levels.handlers.player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.megaman.game.GameEngine;
import com.megaman.game.MegamanGame;
import com.megaman.game.System;
import com.megaman.game.assets.AssetsManager;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.audio.AudioManager;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.megaman.vals.MegamanVals;
import com.megaman.game.entities.megaman.weapons.MegamanWeapon;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventManager;
import com.megaman.game.events.EventType;
import com.megaman.game.screens.utils.BitsBar;
import com.megaman.game.sprites.SpriteSystem;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.TimeMarkedRunnable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.WorldVals;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class PlayerStatsHandler implements Updatable, Drawable {

    private static final float BAR_X = .4f * WorldVals.PPM;
    private static final float BAR_Y = 9f * WorldVals.PPM;
    private static final float DUR_PER_BIT = .2f;

    private final Megaman megaman;
    private final GameEngine engine;
    private final AudioManager audioMan;
    private final EventManager eventMan;

    private final BitsBar healthBar;
    private final Supplier<BitsBar> weaponBarSupplier;

    private OrderedMap<Class<? extends System>, Boolean> sysStates;
    private Timer timer;

    public PlayerStatsHandler(MegamanGame game) {
        megaman = game.getMegaman();
        engine = game.getGameEngine();
        audioMan = game.getAudioMan();
        eventMan = game.getEventMan();
        AssetsManager assMan = game.getAssMan();
        healthBar = new BitsBar(BAR_X, BAR_Y, megaman::getHealth, megaman::getMaxHealth, assMan, "StandardBit");
        Map<MegamanWeapon, BitsBar> weaponBars = new EnumMap<>(MegamanWeapon.class);
        for (MegamanWeapon weapon : MegamanWeapon.values()) {
            BitsBar weaponBar = new BitsBar(BAR_X + WorldVals.PPM, BAR_Y, megaman::getAmmo,
                    () -> MegamanVals.MAX_WEAPON_AMMO, assMan, weapon.weaponBitSrc);
            weaponBars.put(weapon, weaponBar);
        }
        MegamanWeapon curr = megaman.currWeapon;
        weaponBarSupplier = () -> {
            if (curr == null || curr == MegamanWeapon.MEGA_BUSTER) {
                return null;
            } else {
                return weaponBars.get(curr);
            }
        };
    }

    public boolean isFinished() {
        return timer == null || timer.isFinished();
    }

    public void addHealth(int health) {
        if (health < 0) {
            throw new IllegalArgumentException("Health to add cannot be negative");
        }
        int healthNeeded = megaman.maxHealth - megaman.getHealth();
        if (healthNeeded <= 0) {
            return;
        }
        final boolean addToTanks;
        int healthToAdd;
        if (healthNeeded >= health) {
            healthToAdd = health;
            addToTanks = false;
        } else {
            healthToAdd = healthNeeded;
            addToTanks = megaman.addHealthToTanks(health - healthNeeded);
        }
        float dur = healthToAdd * DUR_PER_BIT;
        if (addToTanks) {
            dur += DUR_PER_BIT;
        }
        timer = new Timer(dur, new Array<>() {{
            for (int i = 0; i < healthToAdd; i++) {
                add(new TimeMarkedRunnable(i * DUR_PER_BIT, () -> {
                    megaman.addHealth(1);
                    audioMan.playSound(SoundAsset.ENERGY_FILL_SOUND);
                }));
            }
            if (addToTanks) {
                add(new TimeMarkedRunnable((healthToAdd + 1) * DUR_PER_BIT,
                        () -> audioMan.playSound(SoundAsset.LIFE_SOUND)));
            }
        }});
        sysStates = engine.getStates();
        engine.setAll(false);
        engine.set(true, SpriteSystem.class);
    }

    @Override
    public void update(float delta) {
        if (timer == null) {
            return;
        }
        timer.update(delta);
        if (timer.isJustFinished()) {
            engine.set(sysStates);
            eventMan.submit(new Event(EventType.FINISH_ADD_PLAYER_HEALTH));
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        healthBar.draw(batch);
        BitsBar weaponBar = weaponBarSupplier.get();
        if (weaponBar != null) {
            weaponBar.draw(batch);
        }
    }

}
