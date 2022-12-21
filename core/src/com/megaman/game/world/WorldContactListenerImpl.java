package com.megaman.game.world;

import com.badlogic.gdx.math.Vector2;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.behaviors.BehaviorType;
import com.megaman.game.entities.Damageable;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.megaman.AButtonTask;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.utils.objs.Wrapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorldContactListenerImpl implements WorldContactListener {

    private final MegamanGame game;

    @Override
    public void beginContact(Contact contact, float delta) {
        Wrapper<FixtureType> w = Wrapper.empty();
        if (contact.acceptMask(FixtureType.DAMAGER, FixtureType.DAMAGEABLE)) {
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
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.BLOCK)) {
            contact.mask1stBody().set(BodySense.FEET_ON_GROUND, true);
            if (contact.mask1stEntity() instanceof Megaman megaman) {
                megaman.aButtonTask = AButtonTask.JUMP;
                megaman.request(SoundAsset.MEGAMAN_LAND_SOUND);
            }
        } else if (contact.acceptMask(FixtureType.BOUNCER, w,
                FixtureType.FEET, FixtureType.HEAD, FixtureType.SIDE)) {
            float bounce = contact.mask1stData(ConstKeys.VAL, Float.class);
            Vector2 v = new Vector2();
            switch (w.data) {
                case FEET -> v.y = bounce;
                case HEAD -> v.y = -bounce;
                case SIDE -> {
                    switch (contact.mask2ndData(ConstKeys.SIDE, String.class)) {
                        case ConstKeys.LEFT -> v.x = bounce;
                        case ConstKeys.RIGHT -> v.x = -bounce;
                        default -> throw new IllegalStateException("No side data set");
                    }
                }
                default -> throw new IllegalStateException("Failed to get bounceable fixture type");
            }
            contact.mask2ndBody().velocity.set(v);
            contact.mask2ndData(ConstKeys.RUN, Runnable.class).run();
        } else if (contact.acceptMask(FixtureType.HEAD, FixtureType.BLOCK)) {
            contact.mask1stBody().set(BodySense.HEAD_TOUCHING_BLOCK, true);
        } else if (contact.acceptMask(FixtureType.BODY, FixtureType.WATER)) {
            contact.mask2ndBody().set(BodySense.IN_WATER, true);
            if (contact.mask2ndEntity() instanceof Megaman megaman) {
                megaman.aButtonTask = AButtonTask.SWIM;
            }
            // TODO: relegate to static method in WaterSplash.java
        } else if (contact.acceptMask(FixtureType.BODY, FixtureType.FORCE)) {
            Vector2 force = contact.mask2ndData(ConstKeys.VAL, Vector2.class);
            contact.mask1stBody().velocity.add(force);
        } else if (contact.acceptMask(FixtureType.PROJECTILE, FixtureType.BLOCK)) {
            ((Projectile) contact.mask1stEntity()).hitBlock(contact.mask.getSecond());
        } else if (contact.acceptMask(FixtureType.PROJECTILE, FixtureType.BODY)) {
            ((Projectile) contact.mask1stEntity()).hitBody(contact.mask.getSecond());
        } else if (contact.acceptMask(FixtureType.PROJECTILE, FixtureType.SHIELD)) {
            ((Projectile) contact.mask1stEntity()).hitShield(contact.mask.getSecond());
        }
        // TODO: Add other contacts
    }

    @Override
    public void continueContact(Contact contact, float delta) {
        if (contact.acceptMask(FixtureType.DAMAGER, FixtureType.DAMAGEABLE)) {
            Damager dmgr = (Damager) contact.mask1stEntity();
            Damageable dmgbl = (Damageable) contact.mask2ndEntity();
            if (dmgr.canDamage(dmgbl) && dmgbl.canBeDamagedBy(dmgr)) {
                dmgbl.takeDamageFrom(dmgr);
                dmgr.onDamageInflictedTo(dmgbl);
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.BLOCK)) {
            contact.mask1stBody().set(BodySense.FEET_ON_GROUND, true);
            Vector2 posDelta = contact.mask2ndBody().getPosDelta();
            Body b = contact.mask1stBody();
            b.bounds.x += posDelta.x;
            b.bounds.y += posDelta.y;
            if (contact.mask1stEntity() instanceof Megaman megaman) {
                megaman.aButtonTask = AButtonTask.JUMP;
            }
        } else if (contact.acceptMask(FixtureType.FEET, FixtureType.ICE)) {
            contact.mask1stBody().resistance.x = .95f;
        } else if (contact.acceptMask(FixtureType.SIDE, FixtureType.ICE)) {
            if (contact.mask1stEntity() instanceof Megaman megaman && megaman.is(BehaviorType.WALL_SLIDING)) {
                megaman.body.velocity.set(0f, -12.5f);
            }
        } else if (contact.acceptMask(FixtureType.BODY, FixtureType.FORCE)) {
            Vector2 force = contact.mask2ndData(ConstKeys.VAL, Vector2.class);
            contact.mask1stBody().velocity.add(force);
        } else if (contact.acceptMask(FixtureType.LASER, FixtureType.BLOCK)) {

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
        }
    }

}


