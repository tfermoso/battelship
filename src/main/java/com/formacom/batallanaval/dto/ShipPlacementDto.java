package com.formacom.batallanaval.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipPlacementDto {

    private Integer largeRow;
    private Integer largeCol;
    private String largeOrientation;

    private Integer medium1Row;
    private Integer medium1Col;
    private String medium1Orientation;

    private Integer medium2Row;
    private Integer medium2Col;
    private String medium2Orientation;

    private Integer small1Row;
    private Integer small1Col;

    private Integer small2Row;
    private Integer small2Col;

    private Integer small3Row;
    private Integer small3Col;
}