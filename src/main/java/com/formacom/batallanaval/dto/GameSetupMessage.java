package com.formacom.batallanaval.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSetupMessage {

    private String type; // PLAYER_READY, GAME_START, SHOT
    private Long gameId;
    private Long playerId;
    private String playerName;
    private String content;

    private Integer row;
    private Integer col;
    private String result;
    private String gameStatus;
    private Long currentTurnPlayerId;
}