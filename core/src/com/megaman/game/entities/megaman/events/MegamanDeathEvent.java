package com.megaman.game.entities.megaman.events;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.GameEngine;
import com.megaman.game.assets.SoundAsset;
import com.megaman.game.entities.EntityFactories;
import com.megaman.game.entities.EntityType;
import com.megaman.game.entities.explosions.ExplosionFactory;
import com.megaman.game.entities.megaman.Megaman;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventManager;
import com.megaman.game.events.EventType;
import com.megaman.game.shapes.ShapeUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MegamanDeathEvent implements Runnable {

    public static final float EXPLOSION_ORB_SPEED = 3.5f;

    private final Megaman megaman;

    @Override
    public void run() {
        EventManager eventMan = megaman.game.getEventMan();
        eventMan.remove(megaman);
        eventMan.submit(new Event(EventType.PLAYER_JUST_DIED));
        megaman.game.getAudioMan().stop(SoundAsset.MEGA_BUSTER_CHARGING_SOUND);
        if (megaman.getHealth() > 0f) {
            return;
        }
        Array<Vector2> trajs = new Array<>() {{
            add(new Vector2(-EXPLOSION_ORB_SPEED, 0f));
            add(new Vector2(-EXPLOSION_ORB_SPEED, EXPLOSION_ORB_SPEED));
            add(new Vector2(0f, EXPLOSION_ORB_SPEED));
            add(new Vector2(EXPLOSION_ORB_SPEED, EXPLOSION_ORB_SPEED));
            add(new Vector2(EXPLOSION_ORB_SPEED, 0f));
            add(new Vector2(EXPLOSION_ORB_SPEED, -EXPLOSION_ORB_SPEED));
            add(new Vector2(0f, -EXPLOSION_ORB_SPEED));
            add(new Vector2(-EXPLOSION_ORB_SPEED, -EXPLOSION_ORB_SPEED));
        }};
        GameEngine engine = megaman.game.getGameEngine();
        EntityFactories f = megaman.game.getEntityFactories();
        Vector2 c = ShapeUtils.getCenter(megaman.body.bounds);
        for (Vector2 traj : trajs) {
            engine.spawn(f.fetch(EntityType.EXPLOSION, ExplosionFactory.EXPLOSION_ORB), c, new ObjectMap<>() {{
                put(ConstKeys.TRAJECTORY, traj);
            }});
        }
    }

}
