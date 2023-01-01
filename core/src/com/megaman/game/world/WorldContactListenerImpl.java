package com.megaman.game.world;

import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.controllers.CtrlBtn;
import com.megaman.game.entities.Damageable;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.Entity;
import com.megaman.game.entities.decorations.impl.Splash;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.entities.items.Item;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.megaman.upgrades.MegaAbility;
import com.megaman.game.entities.megaman.vals.AButtonTask;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.entities.sensors.impl.Gate;
import com.megaman.game.entities.special.SpecialFactory;
import com.megaman.game.entities.special.impl.SpringBouncer;
import com.megaman.game.health.HealthComponent;
import com.megaman.game.movement.trajectory.TrajectoryComponent;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.enums.Direction;
import com.megaman.game.utils.objs.Wrapper;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class WorldContactListenerImpl implements WorldContactListener {

    private static final Logger logger = new Logger(WorldContactListener.class, MegamanGame.DEBUG && true);

    private final MegamanGame game;

    @Override
    @SuppressWarnings("unchecked")
    public void beginContact(Contact contact, float delta) {
        Wrapper<FixtureType> w = Wrapper.empty();
        if (contact.acceptMask(FixtureType.CONSUMER, false)) {
            Consumer<Fixture> c = (Consumer<Fixture>) contact.mask1stData(ConstKeys.CONSUMER);
            c.accept(contact.mask.getSecond());
        } else if (contact.acceptMask(FixtureType.DEATH, FixtureType.DAMAGEABLE)) {
            logger.log("Death and damageable begin contact");
            contact.mask2ndEntity().getComponent(HealthComponent.class).setDead();
        } else if (contact.acceptMask(FixtureType.DAMAGER, FixtureType.DAMAGEABLE)) {
            Damager dmgr = (Damager) contact.mask1stEntity();
            Damageable dmgbl = (Damageable) contact.mask2ndEntity();
            if (dmgr.canDamage(dmgbl) && dmgbl.canBeDamagedBy(dmgr)) {
                dmgbl.takeDamageFrom(dmgr);
                dmgr.onDamageInflictedTo(dmgbl);
            }
        } else if (contact.acceptMask(FixtureType.SIDE, FixtureType.BLOCK)) {
            if (contact.mask2ndBody().labels.contains(BodyLabel.NO_TOUCHIE)) {
                return;
            }
            Body body = contact.mask1stBody();
            String side = contact.mask1stData(ConstKeys.SIDE, String.class);
            if (side.equals(ConstKeys.LEFT)) {
                body.set(BodySense.SIDE_TOUCHING_BLOCK_LEFT, true);
            } else {
                body.set(BodySense.SIDE_TOUCHING_BLOCK_RIGHT, true);
            }
        } else if (contact.acceptMask(FixtureType.SIDE, FixtureType.GATE)) {
            if (contact.mask1stEntity() instanceof Megaman &&
                    contact.mask2ndEntity() instanceof Gate gate &&
                    gate.isState(Gate.GateState.OPENABLE)) {
                gate.trigger();
            }
        } else if (contact.acceptMask(FixtureType.SIDE, FixtureType.ICE)) {
            Body body = contact.mask1stBody();
            String side = contact.mask1stData(ConstKeys.SIDE, String.class);
            if (side.equals(ConstKeys.LEFT)) {
                body.set(BodySense.SIDE_TOUCHING_ICE_LEFT, true);
            } else {
                body.set(BodySense.SIDE_TOUCHING_ICE_RIGHT, true);
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.BLOCK)) {
            contact.mask1stBody().set(BodySense.FEET_ON_GROUND, true);
            if (contact.mask1stEntity() instanceof Megaman m) {
                m.aButtonTask = AButtonTask.JUMP;
                m.request(SoundAsset.MEGAMAN_LAND_SOUND, true);
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.ICE)) {
            contact.mask1stBody().set(BodySense.FEET_ON_ICE, true);
        } else if (contact.acceptMask(FixtureType.BOUNCER, w,
                FixtureType.FEET,
                FixtureType.HEAD,
                FixtureType.SIDE)) {
            Vector2 bounce = ((Function<Fixture, Vector2>) contact.mask1stData(ConstKeys.FUNCTION))
                    .apply(contact.mask.getSecond());
            if (contact.mask1stEntity() instanceof SpringBouncer s && contact.mask2ndEntity() instanceof Megaman m) {
                if (!m.is(BodySense.BODY_IN_WATER) && m.has(MegaAbility.AIR_DASH)) {
                    m.aButtonTask = AButtonTask.AIR_DASH;
                }
                if (s.getDir() == Direction.UP && game.getCtrlMan().isPressed(CtrlBtn.DPAD_UP)) {
                    bounce.y *= 2f;
                }
            }
            contact.mask2ndBody().velocity.set(bounce);
            Runnable r = contact.mask1stData(ConstKeys.RUN, Runnable.class);
            if (r != null) {
                r.run();
            }
        } else if (contact.acceptMask(FixtureType.HEAD, FixtureType.BLOCK)) {
            if (!contact.mask2ndBody().labels.contains(BodyLabel.COLLIDE_DOWN_ONLY)) {
                Body headBody = contact.mask1stBody();
                headBody.set(BodySense.HEAD_TOUCHING_BLOCK, true);
                headBody.velocity.y = 0f;
            }
        } else if (contact.acceptMask(FixtureType.WATER_LISTENER, FixtureType.WATER)) {
            contact.mask1stBody().set(BodySense.BODY_IN_WATER, true);
            Entity e = contact.mask1stEntity();
            if (e instanceof Megaman m && !m.is(BodySense.FEET_ON_GROUND) && !m.is(BehaviorType.WALL_SLIDING)) {
                m.aButtonTask = AButtonTask.SWIM;
            }
            Splash.generate(game, contact.mask1stBody(), contact.mask2ndBody());
            if (e instanceof Megaman || e instanceof Enemy) {
                game.getAudioMan().play(SoundAsset.SPLASH_SOUND);
            }
        } else if (contact.acceptMask(FixtureType.HEAD, FixtureType.LADDER)) {
            Body headBody = contact.mask1stBody();
            headBody.set(BodySense.HEAD_TOUCHING_LADDER, true);
            headBody.putUserData(SpecialFactory.LADDER, contact.mask2ndEntity());
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.LADDER)) {
            Body feetBody = contact.mask1stBody();
            feetBody.set(BodySense.FEET_TOUCHING_LADDER, true);
            feetBody.putUserData(SpecialFactory.LADDER, contact.mask2ndEntity());
        } else if (contact.acceptMask(FixtureType.BODY, FixtureType.FORCE)) {
            Vector2 force = ((Function<Fixture, Vector2>) contact.mask2ndData(ConstKeys.FUNCTION))
                    .apply(contact.mask.getFirst());
            contact.mask1stBody().velocity.add(force);
        } else if (contact.acceptMask(FixtureType.PROJECTILE, w,
                FixtureType.BLOCK,
                FixtureType.BODY,
                FixtureType.SHIELD,
                FixtureType.WATER)) {
            if (contact.mask2ndBody().labels.contains(BodyLabel.NO_PROJECTILE_COLLISION)) {
                return;
            }
            Projectile p = (Projectile) contact.mask1stEntity();
            Fixture f = contact.mask.getSecond();
            switch (w.data) {
                case BLOCK -> p.hitBlock(f);
                case BODY -> p.hitBody(f);
                case SHIELD -> p.hitShield(f);
                case WATER -> p.hitWater(f);
            }
        } else if (contact.acceptMask(FixtureType.PLAYER, FixtureType.ITEM)) {
            if (contact.mask1stEntity() instanceof Megaman m && contact.mask2ndEntity() instanceof Item i) {
                i.contactWithPlayer(m);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void continueContact(Contact contact, float delta) {
        if (contact.acceptMask(FixtureType.CONSUMER, false)) {
            Consumer<Fixture> c = (Consumer<Fixture>) contact.mask1stData(ConstKeys.CONSUMER);
            c.accept(contact.mask.getSecond());
        } else if (contact.acceptMask(FixtureType.DAMAGER, FixtureType.DAMAGEABLE)) {
            Damager dmgr = (Damager) contact.mask1stEntity();
            Damageable dmgbl = (Damageable) contact.mask2ndEntity();
            if (dmgr.canDamage(dmgbl) && dmgbl.canBeDamagedBy(dmgr)) {
                dmgbl.takeDamageFrom(dmgr);
                dmgr.onDamageInflictedTo(dmgbl);
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.BLOCK)) {
            contact.mask1stBody().set(BodySense.FEET_ON_GROUND, true);
            Vector2 posDelta;
            if (contact.mask2ndEntity().hasComponent(TrajectoryComponent.class)) {
                posDelta = contact.mask2ndEntity().getComponent(TrajectoryComponent.class).trajectory.getPosDelta();
            } else {
                posDelta = contact.mask2ndBody().getPosDelta();
            }
            Body b = contact.mask1stBody();
            b.bounds.x += posDelta.x;
            b.bounds.y += posDelta.y;
            if (contact.mask1stEntity() instanceof Megaman m) {
                m.aButtonTask = AButtonTask.JUMP;
            }
        } else if (contact.acceptMask(FixtureType.WATER_LISTENER, FixtureType.WATER)) {
            contact.mask1stBody().set(BodySense.BODY_IN_WATER, true);
            if (contact.mask1stEntity() instanceof Megaman m &&
                    !m.is(BodySense.FEET_ON_GROUND) &&
                    !m.is(BehaviorType.WALL_SLIDING)) {
                m.aButtonTask = AButtonTask.SWIM;
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.ICE)) {
            contact.mask1stBody().resistance.x = .925f;
        } else if (contact.acceptMask(FixtureType.BODY, FixtureType.FORCE)) {
            Function<Fixture, Vector2> forceFunc = (Function<Fixture, Vector2>)
                    contact.mask2ndData(ConstKeys.FUNCTION);
            Vector2 force = forceFunc.apply(contact.mask.getFirst());
            contact.mask1stBody().velocity.add(force);
        } else if (contact.acceptMask(FixtureType.LASER, FixtureType.BLOCK) &&
                !contact.mask1stEntity().equals(contact.mask2ndEntity())) {
            Fixture first = contact.mask.getFirst();
            Fixture second = contact.mask.getSecond();
            Collection<Vector2> contactPoints = first.getUserData(ConstKeys.COLLECTION, Collection.class);
            Collection<Vector2> temp = new ArrayList<>();
            if (ShapeUtils.intersectLineRect(
                    (Polyline) first.shape,
                    (Rectangle) second.shape, temp)) {
                contactPoints.addAll(temp);
            }
        }
    }

    @Override
    public void endContact(Contact contact, float delta) {
        if (contact.acceptMask(FixtureType.SIDE, FixtureType.BLOCK)) {
            if (contact.mask2ndBody().labels.contains(BodyLabel.NO_TOUCHIE)) {
                return;
            }
            Body body = contact.mask1stBody();
            String side = contact.mask1stData(ConstKeys.SIDE, String.class);
            if (side.equals(ConstKeys.LEFT)) {
                body.set(BodySense.SIDE_TOUCHING_BLOCK_LEFT, false);
            } else {
                body.set(BodySense.SIDE_TOUCHING_BLOCK_RIGHT, false);
            }
        } else if (contact.acceptMask(FixtureType.SIDE, FixtureType.ICE)) {
            Body body = contact.mask1stBody();
            String side = contact.mask1stData(ConstKeys.SIDE, String.class);
            if (side.equals(ConstKeys.LEFT)) {
                body.set(BodySense.SIDE_TOUCHING_ICE_LEFT, false);
            } else {
                body.set(BodySense.SIDE_TOUCHING_ICE_RIGHT, false);
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.BLOCK)) {
            contact.mask1stBody().set(BodySense.FEET_ON_GROUND, false);
            if (contact.mask1stEntity() instanceof Megaman m) {
                m.aButtonTask = m.is(BodySense.BODY_IN_WATER) ? AButtonTask.SWIM : AButtonTask.AIR_DASH;
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.ICE)) {
            contact.mask1stBody().set(BodySense.FEET_ON_ICE, false);
        } else if (contact.acceptMask(FixtureType.HEAD, FixtureType.BLOCK)) {
            contact.mask1stBody().set(BodySense.HEAD_TOUCHING_BLOCK, false);
        } else if (contact.acceptMask(FixtureType.WATER_LISTENER, FixtureType.WATER)) {
            contact.mask1stBody().set(BodySense.BODY_IN_WATER, false);
            if (contact.mask1stEntity() instanceof Megaman m) {
                m.aButtonTask = AButtonTask.AIR_DASH;
            }
            game.getAudioMan().play(SoundAsset.SPLASH_SOUND);
            Splash.generate(game, contact.mask1stBody(), contact.mask2ndBody());
        } else if (contact.acceptMask(FixtureType.HEAD, FixtureType.LADDER)) {
            Body headBody = contact.mask1stBody();
            headBody.set(BodySense.HEAD_TOUCHING_LADDER, false);
            if (!headBody.is(BodySense.FEET_TOUCHING_LADDER)) {
                headBody.removeUserData(SpecialFactory.LADDER);
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.LADDER)) {
            Body feetBody = contact.mask1stBody();
            feetBody.set(BodySense.FEET_TOUCHING_LADDER, false);
            if (!feetBody.is(BodySense.HEAD_TOUCHING_LADDER)) {
                feetBody.removeUserData(SpecialFactory.LADDER);
            }
        }
    }

}


