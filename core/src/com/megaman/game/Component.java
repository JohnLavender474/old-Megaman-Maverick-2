package com.megaman.game;

import com.megaman.game.utils.interfaces.Resettable;

public interface Component extends Resettable {

    @Override
    default void reset(){
    }

}
