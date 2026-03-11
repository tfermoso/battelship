package com.formacom.batallanaval.model;

public enum GameStatus {
    WAITING,       // creada, esperando segundo jugador
    PREPARING,     // jugadores colocando barcos
    IN_PROGRESS,   // partida en juego
    FINISHED       // partida terminada
}
