package com.bad;

public enum Sound {
    FART("fart.wav"),
    WET_FART("wet-fart.wav"),
    KEBAB("kebab.wav"),
    GOAT("goat.wav"),
    UWU("uwu.wav"),
    FORTNITE("fortnite.wav");

    private final String resourceName;

    Sound(String resNam) {
        resourceName = resNam;
    }

    String getResourceName() {
        return resourceName;
    }
}