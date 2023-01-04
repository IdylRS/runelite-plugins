package com.example;

import lombok.extern.slf4j.Slf4j;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
public abstract class SoundFileManager {
    public static InputStream getSoundStream(Sound sound) throws FileNotFoundException {
        return SoundFileManager.class.getResourceAsStream(sound.getResourceName());
    }
}