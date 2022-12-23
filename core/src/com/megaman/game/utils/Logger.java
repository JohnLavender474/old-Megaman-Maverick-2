package com.megaman.game.utils;

import lombok.*;

@AllArgsConstructor
public class Logger {

    private Class<?> aClass;
    private boolean logging;

    public void log(String s) {
        if (logging) {
            System.out.println("[" + aClass.getSimpleName() + "] : " + s);
        }
    }

}
