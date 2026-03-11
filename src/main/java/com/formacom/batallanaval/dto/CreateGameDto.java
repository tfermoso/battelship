package com.formacom.batallanaval.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGameDto {

    @NotBlank(message = "El nombre de la partida es obligatorio")
    private String name;
}
