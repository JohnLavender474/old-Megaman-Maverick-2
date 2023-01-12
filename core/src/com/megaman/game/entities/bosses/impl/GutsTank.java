package com.megaman.game.entities.bosses.impl;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.animations.AnimationComponent;
import com.megaman.game.entities.DamageNegotiation;
import com.megaman.game.entities.Damager;
import com.megaman.game.entities.enemies.Enemy;
import com.megaman.game.sprites.SpriteComponent;
import com.megaman.game.updatables.UpdatableComponent;
import com.megaman.game.utils.objs.Timer;
import com.megaman.game.world.Body;
import com.megaman.game.world.BodyType;

import java.util.HashMap;
import java.util.Map;

public class GutsTank extends Enemy {

    // size = 10w x 8h
    // move 10 tiles in 8 secs

    private static final float WAIT_DUR = 0f;
    private static final float SHOOT_BULLET_DUR = 0f;
    private static final float SHOOT_BULLET_PAUSE = 0f;
    private static final float SHOOT_MET_DUR = 0f;
    private static final float SHOOT_MET_PAUSE = 0f;
    private static final float SHOOT_BEAM_DUR = 0f;
    private static final float SHOOT_BEAM_PAUSE = 0f;

    private static final float VEL_X = 0f;

    private static final int BULLETS_TO_SHOOT = 6;
    private static final int METS_TO_SHOOT = 6;

    private final Timer waitTimer;
    private final Timer shootMetTimer;
    private final Timer shootMetPause;
    private final Timer shootBulletTimer;
    private final Timer shootBulletPause;
    private final Timer shootBeamTimer;
    private final Timer shootBeamPause;

    public GutsTank(MegamanGame game) {
        super(game, BodyType.ABSTRACT);
        waitTimer = new Timer(WAIT_DUR);
        shootMetTimer = new Timer(SHOOT_MET_DUR);
        shootMetPause = new Timer(SHOOT_MET_PAUSE);
        shootBulletTimer = new Timer(SHOOT_BULLET_DUR);
        shootBulletPause = new Timer(SHOOT_BULLET_PAUSE);
        shootBeamTimer = new Timer(SHOOT_BEAM_DUR);
        shootBeamPause = new Timer(SHOOT_BEAM_PAUSE);
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {

    }

    @Override
    protected Map<Class<? extends Damager>, DamageNegotiation> defineDamageNegotiations() {
        return new HashMap<>() {{

        }};
    }

    @Override
    protected void defineBody(Body body) {

    }

    @Override
    protected void defineUpdateComponent(UpdatableComponent c) {
        super.defineUpdateComponent(c);

    }

    private SpriteComponent spriteComponent() {
        return null;
    }

    private AnimationComponent animationComponent() {
        return null;
    }

}
