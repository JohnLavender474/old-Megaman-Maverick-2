package com.megaman.game.entities.blocks.impl;

import com.megaman.game.MegamanGame;
import com.megaman.game.entities.blocks.Block;
import com.megaman.game.world.BodyType;

public class AbstractBounds extends Block {

    public AbstractBounds(MegamanGame game) {
        super(game, BodyType.ABSTRACT, true);
    }

}
