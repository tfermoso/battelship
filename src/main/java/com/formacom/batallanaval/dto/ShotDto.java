package com.formacom.batallanaval.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShotDto {

    @NotNull
    @Min(0)
    @Max(9)
    private Integer x;

    @NotNull
    @Min(0)
    @Max(9)
    private Integer y;
}