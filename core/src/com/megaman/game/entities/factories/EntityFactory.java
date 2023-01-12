package com.megaman.game.entities.factories;

import com.megaman.game.entities.Entity;

public interface EntityFactory {

    Entity fetch(String key);

}
