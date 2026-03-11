package com.formacom.batallanaval.service;

import com.formacom.batallanaval.dto.CreateGameDto;
import com.formacom.batallanaval.model.Board;
import com.formacom.batallanaval.model.Game;
import com.formacom.batallanaval.model.GameStatus;
import com.formacom.batallanaval.model.User;
import com.formacom.batallanaval.repository.BoardRepository;
import com.formacom.batallanaval.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final BoardRepository boardRepository;

    public List<Game> findAvailableGames() {
        return gameRepository.findByStatus(GameStatus.WAITING);
    }

    public Game createGame(CreateGameDto dto, User player1) {
        Game game = Game.builder()
                .name(dto.getName())
                .code(generateCode())
                .status(GameStatus.WAITING)
                .player1(player1)
                .createdAt(LocalDateTime.now())
                .build();

        return gameRepository.save(game);
    }

    public Game findById(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));
    }

    public Game joinGame(Long gameId, User player2) {
        Game game = findById(gameId);

        if (game.getPlayer1().getId().equals(player2.getId())) {
            throw new RuntimeException("No puedes unirte a tu propia partida");
        }

        if (game.getPlayer2() != null) {
            throw new RuntimeException("La partida ya tiene dos jugadores");
        }

        if (game.getStatus() != GameStatus.WAITING) {
            throw new RuntimeException("La partida ya no está disponible");
        }

        game.setPlayer2(player2);
        game.setStatus(GameStatus.PREPARING);
        game = gameRepository.save(game);

        createBoardsForGame(game);

        return game;
    }

    public void markGameAsInProgress(Game game) {
        game.setStatus(GameStatus.IN_PROGRESS);
        gameRepository.save(game);
    }

    private void createBoardsForGame(Game game) {
        Board board1 = Board.builder()
                .game(game)
                .player(game.getPlayer1())
                .size(10)
                .ready(false)
                .build();

        Board board2 = Board.builder()
                .game(game)
                .player(game.getPlayer2())
                .size(10)
                .ready(false)
                .build();

        boardRepository.save(board1);
        boardRepository.save(board2);
    }

    private String generateCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}