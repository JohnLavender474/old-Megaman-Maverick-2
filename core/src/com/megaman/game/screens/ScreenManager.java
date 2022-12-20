package com.megaman.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ObjectMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScreenManager {

    private final Game game;
    private final ObjectMap<String, Screen> screens = new ObjectMap<>();

    private String currStringKey;

    public void setScreen(String key) {
        game.setScreen(screens.get(key));
    }

}
