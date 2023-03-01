package com.hanannie;

import lombok.extern.slf4j.Slf4j;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
public abstract class SoundFileManager {
    public static InputStream getSoundStream(String sound) throws FileNotFoundException {
        return SoundFileManager.class.getResourceAsStream(sound);
    }
}