package com.pandemicscape;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

@Getter
public class PandemicScapeData {
    private String username;
    private String infectedDateTime;
    private String infectedBy;
    private WorldPoint infectionPoint;
    private boolean isContagious;

    public PandemicScapeData(String username, String infectedDateTime, String infectedBy, WorldPoint infectionPoint, boolean isContagious) {
        this.username = username;
        this.infectedDateTime = infectedDateTime;
        this.infectedBy = infectedBy;
        this.infectionPoint = infectionPoint;
        this.isContagious = isContagious;
    }
}
