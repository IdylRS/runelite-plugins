package com.pandemicscape;

import lombok.Getter;

@Getter
public class PandemicScapeData {
    private String username;
    private String infectedDateTime;
    private String infectedBy;
    private int infectedRegion;
    private int numberInfected;

    public PandemicScapeData(String username, String infectedDateTime, String infectedBy, int infectedRegion) {
        this.username = username;
        this.infectedDateTime = infectedDateTime;
        this.infectedBy = infectedBy;
        this.infectedRegion = infectedRegion;
        this.numberInfected = 0;
    }
}
