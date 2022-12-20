package com.megaman.game.entities.projectiles;

import com.megaman.game.MegamanGame;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.cull.CullOnEventComponent;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.Damageable;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.EntityType;
import com.megaman.game.events.EventType;
import com.megaman.game.world.Body;
import com.megaman.game.world.BodyComponent;
import com.megaman.game.world.BodyType;
import com.megaman.game.world.Fixture;

import java.util.EnumSet;
import java.util.Set;

public abstract class Projectile extends Entity implements Damager {

    public final Body body = new Body(BodyType.ABSTRACT);

    public Entity owner;

    public Projectile(MegamanGame game, Entity owner) {
        super(game, EntityType.PROJECTILE);
        this.owner = owner;
        addComponent(new SoundComponent());
        addComponent(new BodyComponent(body));
        addComponent(cullOnMessageComponent());
        addComponent(new CullOutOfBoundsComponent(() -> body.bounds));
    }

    @Override
    public boolean canDamage(Damageable damageable) {
        return owner == null || !owner.equals(damageable);
    }

    public void hitBody(Fixture bodyFixture) {
    }

    public void hitBlock(Fixture blockFixture) {
    }

    public void hitShield(Fixture shieldFixture) {
    }

    protected CullOnEventComponent cullOnMessageComponent() {
        CullOnEventComponent c = new CullOnEventComponent();
        Set<EventType> s = EnumSet.of(
                EventType.PLAYER_SPAWN,
                EventType.BEGIN_GAME_ROOM_TRANS,
                EventType.GATE_INIT_OPENING);
        c.preds.add(e -> s.contains(e.eventType));
        return c;
    }

}
