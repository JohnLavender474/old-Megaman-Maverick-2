package com.megaman.game.entities.projectiles.impl;

import com.megaman.game.MegamanGame;
import com.megaman.game.entities.Faceable;
import com.megaman.game.entities.Facing;
import com.megaman.game.entities.projectiles.Projectile;
import com.megaman.game.utils.objs.Timer;
import lombok.Getter;
import lombok.Setter;

public class PreciousShot extends Projectile implements Faceable {

    /*
    TODO:
    - Formation: Shield fixture grows while forming
    - Rotate sprite depending on direction (allow diagonal)
    - Create PreciousJoe

    - params:
        - owner: Entity, standard proj param
        - big: boolean, whether this is a "big" shot or not
        - form: boolean, whether to "form" (stand in place undergoin form anim)
        - traj: Vector2, trajectory of shot

    - special:
        - if hit block when big, shatter: three shots, one up diag, one straight back, and other down diag
        - if hit block when small, die
        - if hit shield that can be broken, then damage or destroy the shield
     */

    private static final float FORM_DUR = 1f;

    private final Timer formTimer;

    @Getter
    @Setter
    private Facing facing;

    public PreciousShot(MegamanGame game) {
        super(game);
        formTimer = new Timer(FORM_DUR);
    }



}
