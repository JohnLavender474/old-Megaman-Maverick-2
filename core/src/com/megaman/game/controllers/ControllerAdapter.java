package com.megaman.game.controllers;

public interface ControllerAdapter {

    default void onJustPressed() {
    }

    default void onPressContinued(float delta) {
    }

    default void onJustReleased() {
    }

    default void onReleaseContinued() {
    }

}
