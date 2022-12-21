package com.megaman.game.entities.enemies;

import com.megaman.game.MegamanGame;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.cull.CullOnEventComponent;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.*;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.events.EventType;
import com.megaman.game.health.HealthComponent;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.interfaces.Updatable;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Body;
import com.megaman.game.world.BodyComponent;
import com.megaman.game.world.BodyType;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public abstract class Enemy extends Entity implements Damager, Damageable, Updatable {

    public static final float DEFAULT_CULL_DUR = 1.5f;

    protected final Timer dmgTimer;
    protected final Map<Class<? extends Damager>, DamageNegotiation> dmgNegs;

    protected final Body body = new Body(BodyType.DYNAMIC);

    public Enemy(MegamanGame game, float damageDuration) {
        this(game, damageDuration, DEFAULT_CULL_DUR);
    }

    public Enemy(MegamanGame game, float dmgDur, float cullDur) {
        super(game, EntityType.ENEMY);
        this.dmgTimer = new Timer(dmgDur, true);
        this.dmgNegs = defineDamageNegotiations();
        // define body comp
        defineBody(body);
        addComponent(new BodyComponent(body));
        // define updatable comp
        UpdatableComponent uc = new UpdatableComponent();
        defineUpdateComponent(uc);
        addComponent(uc);
        // define cull event comp
        CullOnEventComponent coec = new CullOnEventComponent();
        defineCullOnEventComponent(coec);
        addComponent(coec);
        // define cull oocb comp
        CullOutOfBoundsComponent coocbc = new CullOutOfBoundsComponent();
        coocbc.timer = new Timer(cullDur);
        coocbc.boundsSupplier = () -> body.bounds;
        addComponent(coocbc);
        // other comps
        addComponent(new SoundComponent());
        addComponent(new HealthComponent(this::disintegrate));
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
        getComponent(SoundComponent.class).request(SoundAsset.ENEMY_DAMAGE_SOUND);
    }

    protected void disintegrate() {
        getComponent(SoundComponent.class).request(SoundAsset.ENEMY_DAMAGE_SOUND);
        // gameContext.addEntity(new Disintegration(gameContext, getComponent(BodyComponent.class).getCenter()));
    }

    protected void explode() {
        getComponent(SoundComponent.class).request(SoundAsset.EXPLOSION_SOUND);
        // gameContext.addEntity(new Explosion(gameContext, getComponent(BodyComponent.class).getCenter()));
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
