package com.formacom.batallanaval.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlacedShipDto {
    private String type;          // LARGE, MEDIUM, SMALL
    private Integer row;
    private Integer col;
    private String orientation;   // HORIZONTAL, VERTICAL
}