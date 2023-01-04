package com.megaman.game.entities.blocks.impl;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;
import com.megaman.game.MegamanGame;
import com.megaman.game.entities.blocks.Block;
import com.megaman.game.world.BodyLabel;

public class CollideDownOnlyBlock extends Block {

    public CollideDownOnlyBlock(MegamanGame game) {
        super(game, true);
    }

    @Override
    public void init(Rectangle bounds, ObjectMap<String, Object> data) {
        super.init(bounds, data);
        body.labels.add(BodyLabel.NO_TOUCHIE);
        body.labels.add(BodyLabel.COLLIDE_DOWN_ONLY);
        body.labels.add(BodyLabel.PRESS_UP_FALL_THRU);
    }

}
