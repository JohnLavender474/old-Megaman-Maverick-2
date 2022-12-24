package com.megaman.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setIdleFPS(60);
        config.setForegroundFPS(60);
        config.setWindowedMode(1920, 1080);
        config.setTitle("MegamanMaverick");
        MegamanGame game = new MegamanGame();
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void iconified(boolean isIconified) {
                game.pause();
            }

            @Override
            public void focusGained() {
                game.resume();
            }

            @Override
            public void focusLost() {
                game.pause();
            }
        });
        new Lwjgl3Application(game, config);
    }
}
