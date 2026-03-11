package com.formacom.batallanaval.websocket;


import com.formacom.batallanaval.dto.GameSetupMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendPlayerReady(Long gameId, Long playerId, String playerName) {
        GameSetupMessage message = GameSetupMessage.builder()
                .type("PLAYER_READY")
                .gameId(gameId)
                .playerId(playerId)
                .playerName(playerName)
                .content(playerName + " ya está listo")
                .build();

        messagingTemplate.convertAndSend("/topic/game/" + gameId, message);
    }

    public void sendGameStart(Long gameId) {
        GameSetupMessage message = GameSetupMessage.builder()
                .type("GAME_START")
                .gameId(gameId)
                .content("La partida comienza")
                .build();

        messagingTemplate.convertAndSend("/topic/game/" + gameId, message);
    }
}
