package com.megaman.game.entities.impl.megaman.upgrades;

public enum MegaHeartTank {

    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H;

    public static final int HEALTH_BUMP = 2;

    public static MegaHeartTank get(String s) {
        return switch (s) {
            case "A" -> A;
            case "B" -> B;
            case "C" -> C;
            case "D" -> D;
            case "E" -> E;
            case "F" -> F;
            case "G" -> G;
            case "H" -> H;
            default -> throw new IllegalArgumentException("No matching heart tank: " + s);
        };
    }

}
