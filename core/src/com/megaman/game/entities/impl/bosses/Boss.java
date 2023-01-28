package com.megaman.game.entities.impl.bosses;

import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.ConstKeys;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.impl.enemies.Enemy;
import com.megaman.game.events.Event;
import com.megaman.game.events.EventType;
import com.megaman.game.world.BodyType;

public abstract class Boss extends Enemy {

    public Boss(MegamanGame game) {
        this(game, BodyType.DYNAMIC);
    }

    public Boss(MegamanGame game, BodyType bodyType) {
        super(game, bodyType);
        doDropItem = false;
        runOnDeath.add(() -> {
            ObjectMap<String, Object> data = new ObjectMap<>();
            setDeathEventData(data);
            game.getEventMan().submit(new Event(EventType.BOSS_DEAD, data));
        });
    }

    protected void onSpawn() {
        ObjectMap<String, Object> data = new ObjectMap<>();
        setSpawnEventData(data);
        game.getEventMan().submit(new Event(EventType.BOSS_SPAWN, data));
    }

    protected void setSpawnEventData(ObjectMap<String, Object> data) {
        data.put(ConstKeys.ENTITY, Boss.this);
    }

    protected void setDeathEventData(ObjectMap<String, Object> data) {
        data.put(ConstKeys.ENTITY, Boss.this);
    }

}
