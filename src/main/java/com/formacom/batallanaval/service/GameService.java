package com.formacom.batallanaval.service;

import com.formacom.batallanaval.dto.CreateGameDto;
import com.formacom.batallanaval.model.*;
import com.formacom.batallanaval.repository.BoardRepository;
import com.formacom.batallanaval.repository.GameRepository;
import com.formacom.batallanaval.repository.ShotRepository;
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
    private final ShotRepository shotRepository;
    private final BoardService boardService;

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

    public List<Game> findGamesForUser(User user) {
        return gameRepository.findByPlayer1OrPlayer2(user, user);
    }

    public List<Game> findGamesForUserByStatus(User user, GameStatus status) {
        return gameRepository.findByPlayer1OrPlayer2(user, user)
                .stream()
                .filter(game -> game.getStatus() == status)
                .toList();
    }

    public List<Game> findJoinableGames(User user) {
        return gameRepository.findByStatus(GameStatus.WAITING)
                .stream()
                .filter(game -> !game.getPlayer1().getId().equals(user.getId()))
                .toList();
    }

    public User getCurrentTurnPlayer(Game game) {
        long totalShots = shotRepository.countByGame(game);
        return totalShots % 2 == 0 ? game.getPlayer1() : game.getPlayer2();
    }

    public User getOpponent(Game game, User user) {
        if (game.getPlayer1().getId().equals(user.getId())) {
            return game.getPlayer2();
        }
        if (game.getPlayer2().getId().equals(user.getId())) {
            return game.getPlayer1();
        }
        throw new RuntimeException("El usuario no pertenece a esta partida");
    }

    public boolean isUsersTurn(Game game, User user) {
        return getCurrentTurnPlayer(game).getId().equals(user.getId());
    }

    public List<Shot> getShotsByShooter(Game game, User shooter) {
        return shotRepository.findByGameAndShooter(game, shooter);
    }

    public List<Shot> getAllShots(Game game) {
        return shotRepository.findByGameOrderByCreatedAtAsc(game);
    }

    public void shoot(Game game, User shooter, int row, int col) {
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new RuntimeException("La partida no está en curso");
        }

        if (!isUsersTurn(game, shooter)) {
            throw new RuntimeException("No es tu turno");
        }

        User targetUser = getOpponent(game, shooter);
        Board targetBoard = boardService.findByGameAndPlayer(game, targetUser);

        if (shotRepository.existsByGameAndShooterAndRowAndCol(game, shooter, row, col)) {
            throw new RuntimeException("Ya has disparado a esa posición");
        }

        boolean hit = boardService.hasShipAt(targetBoard, row, col);
        ShotResult result = hit ? ShotResult.HIT : ShotResult.SUNK;

        Shot shot = Shot.builder()
                .game(game)
                .shooter(shooter)
                .targetBoard(targetBoard)
                .row(row)
                .col(col)
                .result(result)
                .createdAt(LocalDateTime.now())
                .build();

        shotRepository.save(shot);

        if (hasPlayerLost(game, targetUser)) {
            game.setStatus(GameStatus.FINISHED);
            gameRepository.save(game);
        }
    }

    public boolean hasPlayerLost(Game game, User player) {
        Board board = boardService.findByGameAndPlayer(game, player);

        long totalShipCells = boardService.countOccupiedCells(board);

        long hitsReceived = shotRepository.findByGameAndTargetBoard(game, board)
                .stream()
                .filter(shot -> shot.getResult() == ShotResult.HIT || shot.getResult() == ShotResult.SUNK)
                .count();

        return totalShipCells > 0 && hitsReceived >= totalShipCells;
    }

    public User getWinner(Game game) {
        if (game.getStatus() != GameStatus.FINISHED) {
            return null;
        }

        if (hasPlayerLost(game, game.getPlayer1())) {
            return game.getPlayer2();
        }

        if (hasPlayerLost(game, game.getPlayer2())) {
            return game.getPlayer1();
        }

        return null;
    }
}