package com.megaman.game.entities.enemies;

import com.megaman.game.MegamanGame;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.cull.CullOnEventComponent;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.*;
import com.megaman.game.entities.explosions.ExplosionFactory;
import com.megaman.game.entities.explosions.impl.Disintegration;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.events.EventType;
import com.megaman.game.health.HealthComponent;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Body;
import com.megaman.game.world.BodyComponent;
import com.megaman.game.world.BodySense;
import com.megaman.game.world.BodyType;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public abstract class Enemy extends Entity implements Damager, Damageable {

    private static final float DEFAULT_CULL_DUR = .75f;
    private static final float DEFAULT_DMG_DUR = .15f;

    protected final Body body;
    protected final Timer dmgTimer;
    protected final Map<Class<? extends Damager>, DamageNegotiation> dmgNegs;

    public Enemy(MegamanGame game, BodyType bodyType) {
        this(game, DEFAULT_DMG_DUR, bodyType);
    }

    public Enemy(MegamanGame game, float damageDuration, BodyType bodyType) {
        this(game, damageDuration, DEFAULT_CULL_DUR, bodyType);
    }

    public Enemy(MegamanGame game, float dmgDur, float cullDur, BodyType bodyType) {
        super(game, EntityType.ENEMY);
        body = new Body(bodyType);
        dmgTimer = new Timer(dmgDur, true);
        dmgNegs = defineDamageNegotiations();
        defineBody(body);
        putComponent(new BodyComponent(body));
        UpdatableComponent u = new UpdatableComponent();
        defineUpdateComponent(u);
        putComponent(u);
        CullOnEventComponent c = new CullOnEventComponent();
        defineCullOnEventComponent(c);
        putComponent(c);
        putComponent(new SoundComponent());
        putComponent(new HealthComponent());
        putComponent(new CullOutOfBoundsComponent(() -> body.bounds, cullDur));
        runOnDeath.add(() -> {
            if (getHealth() == 0) {
                disintegrate();
            }
        });
    }

    protected abstract Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations();

    protected abstract void defineBody(Body body);

    protected void defineUpdateComponent(UpdatableComponent c) {
        c.add(dmgTimer::update);
    }

    protected void defineCullOnEventComponent(CullOnEventComponent c) {
        Set<EventType> s = EnumSet.of(
                EventType.PLAYER_SPAWN,
                EventType.BEGIN_GAME_ROOM_TRANS,
                EventType.GATE_INIT_OPENING);
        c.preds.add(e -> s.contains(e.eventType));
    }

    @Override
    public Set<Class<? extends Damager>> getDamagerMaskSet() {
        return dmgNegs.keySet();
    }

    public int getHealth() {
        return getComponent(HealthComponent.class).getHealth();
    }

    public boolean isDamaged() {
        return !dmgTimer.isFinished();
    }

    @Override
    public boolean isInvincible() {
        return !dmgTimer.isFinished();
    }

    @Override
    public void takeDamageFrom(Damager damager) {
        DamageNegotiation dmgNeg = dmgNegs.get(damager.getClass());
        if (dmgNeg == null) {
            return;
        }
        dmgTimer.reset();
        dmgNeg.runOnDamage();
        getComponent(HealthComponent.class).translateHealth(-dmgNeg.getDamage(damager));
        getComponent(SoundComponent.class).requestToPlay(SoundAsset.ENEMY_DAMAGE_SOUND);
    }

    public boolean is(BodySense sense) {
        return body.is(sense);
    }

    protected void disintegrate() {
        game.getAudioMan().playSound(SoundAsset.ENEMY_DAMAGE_SOUND);
        game.getGameEngine().spawnEntity(game.getEntityFactories()
                .fetch(EntityType.EXPLOSION, ExplosionFactory.DISINTEGRATION), body.getCenter());
    }

    protected void explode() {
        game.getAudioMan().playSound(SoundAsset.EXPLOSION_SOUND);
        game.getGameEngine().spawnEntity(game.getEntityFactories()
                .fetch(EntityType.EXPLOSION, ExplosionFactory.EXPLOSION), body.getCenter());
    }

    protected boolean isPlayerShootingAtMe() {
        Megaman m = game.getMegaman();
        if (!m.isShooting()) {
            return false;
        }
        BodyComponent bc = getComponent(BodyComponent.class);
        BodyComponent mbc = m.getComponent(BodyComponent.class);
        return (bc.body.bounds.x < mbc.body.bounds.x && m.is(Facing.LEFT)) ||
                (bc.body.bounds.x > mbc.body.bounds.x && m.is(Facing.RIGHT));
    }

}
