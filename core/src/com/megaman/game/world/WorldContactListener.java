package com.megaman.game.world;

public interface WorldContactListener {

    void beginContact(Contact contact, float delta);

    void continueContact(Contact contact, float delta);

    void endContact(Contact contact, float delta);

}
