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

    private String type;       // PLAYER_READY, GAME_START
    private Long gameId;
    private Long playerId;
    private String playerName;
    private String content;
}
