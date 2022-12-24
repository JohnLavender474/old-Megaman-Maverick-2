package com.megaman.game.world;

import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.controllers.ControllerBtn;
import com.megaman.game.entities.Damageable;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.decorations.impl.Splash;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.megaman.vals.AButtonTask;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.movement.trajectory.TrajectoryComponent;
import com.megaman.game.shapes.ShapeUtils;
import com.megaman.game.utils.Logger;
import com.megaman.game.utils.objs.Wrapper;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class WorldContactListenerImpl implements WorldContactListener {

    private static final Logger logger = new Logger(WorldContactListener.class, MegamanGame.DEBUG && false);

    private final MegamanGame game;

    // TODO: Death and Body contact

    @Override
    @SuppressWarnings("unchecked")
    public void beginContact(Contact contact, float delta) {
        Wrapper<FixtureType> w = Wrapper.empty();
        if (contact.acceptMask(FixtureType.SCANNER, false)) {
            Consumer<Fixture> c = (Consumer<Fixture>) contact.mask1stData(ConstKeys.CONSUMER);
            c.accept(contact.mask.getSecond());
        } else if (contact.acceptMask(FixtureType.DAMAGER, FixtureType.DAMAGEABLE)) {
            Damager dmgr = (Damager) contact.mask1stEntity();
            Damageable dmgbl = (Damageable) contact.mask2ndEntity();
            if (dmgr.canDamage(dmgbl) && dmgbl.canBeDamagedBy(dmgr)) {
                dmgbl.takeDamageFrom(dmgr);
                dmgr.onDamageInflictedTo(dmgbl);
            }
        } else if (contact.acceptMask(FixtureType.SIDE, FixtureType.BLOCK)) {
            Body body = contact.mask1stBody();
            String side = contact.mask1stData(ConstKeys.SIDE, String.class);
            if (side.equals(ConstKeys.LEFT)) {
                body.set(BodySense.TOUCHING_BLOCK_LEFT, true);
            } else {
                body.set(BodySense.TOUCHING_BLOCK_RIGHT, true);
            }
        } else if (contact.acceptMask(FixtureType.SIDE, FixtureType.ICE)) {
            Body body = contact.mask1stBody();
            String side = contact.mask1stData(ConstKeys.SIDE, String.class);
            if (side.equals(ConstKeys.LEFT)) {
                body.set(BodySense.TOUCHING_ICE_LEFT, true);
            } else {
                body.set(BodySense.TOUCHING_ICE_RIGHT, true);
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.BLOCK)) {
            contact.mask1stBody().set(BodySense.FEET_ON_GROUND, true);
            if (contact.mask1stEntity() instanceof Megaman megaman) {
                megaman.aButtonTask = AButtonTask.JUMP;
                megaman.request(SoundAsset.MEGAMAN_LAND_SOUND, true);
            }
        } else if (contact.acceptMask(FixtureType.BOUNCER, w,
                FixtureType.FEET,
                FixtureType.HEAD,
                FixtureType.SIDE)) {
            Vector2 bounce = contact.mask1stData(ConstKeys.VAL, Vector2.class).cpy();
            if (w.data == FixtureType.FEET && contact.mask2ndEntity() instanceof Megaman &&
                    game.getCtrlMan().isPressed(ControllerBtn.DPAD_UP)) {
                bounce.y *= 2f;
            }
            contact.mask2ndBody().velocity.set(bounce);
            Runnable r = contact.mask1stData(ConstKeys.RUN, Runnable.class);
            if (r != null) {
                r.run();
            }
        } else if (contact.acceptMask(FixtureType.HEAD, FixtureType.BLOCK)) {
            Body headBody = contact.mask1stBody();
            headBody.set(BodySense.HEAD_TOUCHING_BLOCK, true);
            headBody.velocity.y = 0f;
        } else if (contact.acceptMask(FixtureType.BODY, FixtureType.WATER)) {
            contact.mask1stBody().set(BodySense.IN_WATER, true);
            if (contact.mask1stEntity() instanceof Megaman megaman &&
                    !megaman.is(BodySense.FEET_ON_GROUND) &&
                    !megaman.is(BehaviorType.WALL_SLIDING)) {
                megaman.aButtonTask = AButtonTask.SWIM;
            }
            Splash.generate(game, contact.mask1stBody(), contact.mask2ndBody());
            game.getAudioMan().playSound(SoundAsset.SPLASH_SOUND);
        } else if (contact.acceptMask(FixtureType.BODY, FixtureType.FORCE)) {
            Vector2 force = ((Function<Fixture, Vector2>) contact.mask2ndData(ConstKeys.FUNCTION))
                    .apply(contact.mask.getFirst());
            contact.mask1stBody().velocity.add(force);
        } else if (contact.acceptMask(FixtureType.PROJECTILE, w,
                FixtureType.BLOCK,
                FixtureType.BODY,
                FixtureType.SHIELD,
                FixtureType.WATER)) {
            Projectile p = (Projectile) contact.mask1stEntity();
            Fixture f = contact.mask.getSecond();
            switch (w.data) {
                case BLOCK -> p.hitBlock(f);
                case BODY -> p.hitBody(f);
                case SHIELD -> p.hitShield(f);
                case WATER -> p.hitWater(f);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void continueContact(Contact contact, float delta) {
        if (contact.acceptMask(FixtureType.SCANNER, false)) {
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
            if (contact.mask1stEntity() instanceof Megaman megaman) {
                megaman.aButtonTask = AButtonTask.JUMP;
            }
        } else if (contact.acceptMask(FixtureType.BODY, FixtureType.WATER)) {
            contact.mask1stBody().set(BodySense.IN_WATER, true);
            if (contact.mask1stEntity() instanceof Megaman megaman &&
                    !megaman.is(BodySense.FEET_ON_GROUND) &&
                    !megaman.is(BehaviorType.WALL_SLIDING)) {
                megaman.aButtonTask = AButtonTask.SWIM;
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.ICE)) {
            contact.mask1stBody().resistance.x = .925f;
        } else if (contact.acceptMask(FixtureType.BODY, FixtureType.FORCE)) {
            Function<Fixture, Vector2> forceFunc = (Function<Fixture, Vector2>) contact.mask2ndData(ConstKeys.FUNCTION);
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
            Body body = contact.mask1stBody();
            String side = contact.mask1stData(ConstKeys.SIDE, String.class);
            if (side.equals(ConstKeys.LEFT)) {
                body.set(BodySense.TOUCHING_BLOCK_LEFT, false);
            } else {
                body.set(BodySense.TOUCHING_BLOCK_RIGHT, false);
            }
        } else if (contact.acceptMask(FixtureType.SIDE, FixtureType.ICE)) {
            Body body = contact.mask1stBody();
            String side = contact.mask1stData(ConstKeys.SIDE, String.class);
            if (side.equals(ConstKeys.LEFT)) {
                body.set(BodySense.TOUCHING_ICE_LEFT, false);
            } else {
                body.set(BodySense.TOUCHING_ICE_RIGHT, false);
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.BLOCK)) {
            contact.mask1stBody().set(BodySense.FEET_ON_GROUND, false);
            if (contact.mask1stEntity() instanceof Megaman megaman) {
                megaman.aButtonTask = megaman.is(BodySense.IN_WATER) ? AButtonTask.SWIM : AButtonTask.AIR_DASH;
            }
        } else if (contact.acceptMask(FixtureType.HEAD, FixtureType.BLOCK)) {
            contact.mask1stBody().set(BodySense.HEAD_TOUCHING_BLOCK, false);
        } else if (contact.acceptMask(FixtureType.BODY, FixtureType.WATER)) {
            contact.mask1stBody().set(BodySense.IN_WATER, false);
            if (contact.mask1stEntity() instanceof Megaman megaman) {
                megaman.aButtonTask = AButtonTask.AIR_DASH;
            }
            game.getAudioMan().playSound(SoundAsset.SPLASH_SOUND);
            Splash.generate(game, contact.mask1stBody(), contact.mask2ndBody());
        }
    }

}


