package com.megaman.game.entities.impl.projectiles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.audio.SoundComponent;
import com.megaman.game.cull.CullOnEventComponent;
import com.megaman.game.cull.CullOutOfBoundsComponent;
import com.megaman.game.entities.damage.Damageable;
import com.megaman.game.entities.damage.Damager;
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

    public static final float DEFAULT_CULL_DUR = .5f;

    public final Body body;
    public final Sprite sprite;

    public Entity owner;

    public Projectile(MegamanGame game) {
        this(game, BodyType.ABSTRACT);
    }

    public Projectile(MegamanGame game, BodyType bodyType) {
        this(game, DEFAULT_CULL_DUR, bodyType);
    }

    public Projectile(MegamanGame game, float cullDur, BodyType bodyType) {
        super(game, EntityType.PROJECTILE);
        body = new Body(bodyType);
        sprite = new Sprite();
        putComponent(new SoundComponent());
        putComponent(new BodyComponent(body));
        putComponent(cullOnEventComponent());
        putComponent(new CullOutOfBoundsComponent(() -> body.bounds, cullDur));
    }

    @Override
    public void init(Vector2 spawn, ObjectMap<String, Object> data) {
        owner = (Entity) data.get(ConstKeys.OWNER);
        body.bounds.setCenter(spawn);
    }

    @Override
    public boolean canDamage(Damageable damageable) {
        return !damageable.isInvincible() && !damageable.equals(owner) &&
                damageable instanceof Entity e && entityType != e.entityType;
    }

    public void hitBody(Fixture bodyFixture) {
    }

    public void hitBlock(Fixture blockFixture) {
    }

    public void hitShield(Fixture shieldFixture) {
    }

    public void hitWater(Fixture waterFixture) {
    }

    protected CullOnEventComponent cullOnEventComponent() {
        Set<EventType> s = EnumSet.of(
                EventType.PLAYER_SPAWN,
                EventType.BEGIN_ROOM_TRANS,
                EventType.GATE_INIT_OPENING);
        return new CullOnEventComponent(e -> s.contains(e.type));
    }

}
