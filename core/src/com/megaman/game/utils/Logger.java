package com.megaman.game.utils;

import lombok.*;

@AllArgsConstructor
public class Logger {

    private boolean logging;

    public void log(String s) {
        if (logging) {
            System.out.println(s);
        }
    }

}
