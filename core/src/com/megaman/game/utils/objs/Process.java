package com.megaman.game.utils.objs;

import com.game.utils.interfaces.Updatable;

public record Process(Runnable initRunnable, Updatable actUpdatable, Runnable endRunnable) {}
