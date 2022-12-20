package com.megaman.game.entities;

public interface EntityFactory<E extends Entity> {

    E fetch(String key);

}
