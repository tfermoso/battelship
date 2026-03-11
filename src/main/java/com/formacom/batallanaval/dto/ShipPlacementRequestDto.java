package com.formacom.batallanaval.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ShipPlacementRequestDto {
    private List<PlacedShipDto> ships = new ArrayList<>();
}