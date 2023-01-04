package com.example;

public enum Sound {
    FART("fart.wav"),
    KEBAB("kebab.wav");

    private final String resourceName;

    Sound(String resNam) {
        resourceName = resNam;
    }

    String getResourceName() {
        return resourceName;
    }
}