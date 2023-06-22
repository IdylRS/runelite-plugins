package com.example;

import java.util.TimeZone;

public enum ServerZone {
    BST(TimeZone.getTimeZone("GMT")),
    EDT(TimeZone.getTimeZone("EST")),
    PDT(TimeZone.getTimeZone("PST"));

    public TimeZone timezone;

    ServerZone(TimeZone tz) {
        this.timezone = tz;
    }
}
