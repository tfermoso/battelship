package com.formacom.batallanaval.service;

import com.formacom.batallanaval.dto.PlacedShipDto;
import com.formacom.batallanaval.dto.ShipPlacementRequestDto;
import com.formacom.batallanaval.model.*;
import com.formacom.batallanaval.repository.BoardRepository;
import com.formacom.batallanaval.repository.GameRepository;
import com.formacom.batallanaval.repository.ShipPositionRepository;
import com.formacom.batallanaval.repository.ShipRepository;
import com.formacom.batallanaval.repository.ShotRepository;
import com.formacom.batallanaval.websocket.GameWebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final GameRepository gameRepository;
    private final ShipRepository shipRepository;
    private final ShipPositionRepository shipPositionRepository;
    private final ShotRepository shotRepository;
    private final GameWebSocketService gameWebSocketService;

    public Board getBoardForPlayer(Game game, User player) {
        return boardRepository.findByGameAndPlayer(game, player)
                .orElseThrow(() -> new RuntimeException("Tablero no encontrado"));
    }

    public Board findByGameAndPlayer(Game game, User player) {
        return getBoardForPlayer(game, player);
    }

    public List<Board> getBoardsForGame(Game game) {
        return boardRepository.findByGame(game);
    }

    @Transactional
    public void saveShipPlacement(Game game, User player, ShipPlacementRequestDto dto) {
        Board board = getBoardForPlayer(game, player);

        validateFleet(dto);
        validatePlacement(board, dto.getShips());

        clearBoard(board);
        createShips(board, dto.getShips());

        board.setReady(true);
        boardRepository.save(board);

        gameWebSocketService.sendPlayerReady(game.getId(), player.getId(), player.getName());

        checkIfBothBoardsAreReady(game);
    }

    private void validateFleet(ShipPlacementRequestDto dto) {
        if (dto.getShips() == null || dto.getShips().size() != 6) {
            throw new RuntimeException("Debes colocar exactamente 6 barcos");
        }

        long large = dto.getShips().stream().filter(s -> "LARGE".equals(s.getType())).count();
        long medium = dto.getShips().stream().filter(s -> "MEDIUM".equals(s.getType())).count();
        long small = dto.getShips().stream().filter(s -> "SMALL".equals(s.getType())).count();

        if (large != 1 || medium != 2 || small != 3) {
            throw new RuntimeException("La flota debe ser: 1 grande, 2 medianos y 3 pequeños");
        }
    }

    private void validatePlacement(Board board, List<PlacedShipDto> ships) {
        Set<String> occupied = new HashSet<>();
        int boardSize = board.getSize();

        for (PlacedShipDto ship : ships) {
            ShipType type = ShipType.valueOf(ship.getType());
            int size = getShipSize(type);

            if (ship.getRow() == null || ship.getCol() == null) {
                throw new RuntimeException("Todos los barcos deben tener fila y columna");
            }

            Orientation orientation = Orientation.HORIZONTAL;
            if (size > 1) {
                if (ship.getOrientation() == null || ship.getOrientation().isBlank()) {
                    throw new RuntimeException("La orientación es obligatoria para barcos grandes y medianos");
                }
                orientation = Orientation.valueOf(ship.getOrientation());
            }

            for (int i = 0; i < size; i++) {
                int currentRow = ship.getRow();
                int currentCol = ship.getCol();

                if (size > 1) {
                    if (orientation == Orientation.HORIZONTAL) {
                        currentCol += i;
                    } else {
                        currentRow += i;
                    }
                }

                if (currentRow < 0 || currentRow >= boardSize || currentCol < 0 || currentCol >= boardSize) {
                    throw new RuntimeException("Hay barcos colocados fuera del tablero");
                }

                String key = currentRow + "-" + currentCol;
                if (!occupied.add(key)) {
                    throw new RuntimeException("Los barcos no pueden solaparse");
                }
            }
        }
    }

    private void clearBoard(Board board) {
        List<Ship> ships = shipRepository.findByBoard(board);
        shipPositionRepository.deleteAll(shipPositionRepository.findByBoard(board));
        shipRepository.deleteAll(ships);
        board.setReady(false);
        boardRepository.save(board);
    }

    private void createShips(Board board, List<PlacedShipDto> placedShips) {
        for (PlacedShipDto placedShip : placedShips) {
            ShipType type = ShipType.valueOf(placedShip.getType());
            int size = getShipSize(type);

            Ship ship = Ship.builder()
                    .type(type)
                    .size(size)
                    .sunk(false)
                    .board(board)
                    .build();

            ship = shipRepository.save(ship);

            Orientation orientation = Orientation.HORIZONTAL;
            if (size > 1) {
                orientation = Orientation.valueOf(placedShip.getOrientation());
            }

            for (int i = 0; i < size; i++) {
                int currentRow = placedShip.getRow();
                int currentCol = placedShip.getCol();

                if (size > 1) {
                    if (orientation == Orientation.HORIZONTAL) {
                        currentCol += i;
                    } else {
                        currentRow += i;
                    }
                }

                ShipPosition position = ShipPosition.builder()
                        .row(currentRow)
                        .col(currentCol)
                        .hit(false)
                        .ship(ship)
                        .board(board)
                        .build();

                shipPositionRepository.save(position);
            }
        }
    }

    private int getShipSize(ShipType type) {
        return switch (type) {
            case LARGE -> 4;
            case MEDIUM -> 2;
            case SMALL -> 1;
        };
    }

    private void checkIfBothBoardsAreReady(Game game) {
        List<Board> boards = boardRepository.findByGame(game);

        boolean allReady = boards.size() == 2 &&
                boards.stream().allMatch(board -> Boolean.TRUE.equals(board.getReady()));

        if (allReady) {
            game.setStatus(GameStatus.IN_PROGRESS);
            gameRepository.save(game);
            gameWebSocketService.sendGameStart(game.getId());
        }
    }

    public String[][] getBoardMatrix(Game game, User user) {
        Board board = findByGameAndPlayer(game, user);
        int size = board.getSize() != null ? board.getSize() : 10;

        String[][] matrix = createMatrix(size, "W");

        List<ShipPosition> positions = shipPositionRepository.findByBoard(board);
        for (ShipPosition position : positions) {
            matrix[position.getRow()][position.getCol()] = "S";
        }

        List<Shot> receivedShots = shotRepository.findByGameAndTargetBoard(game, board);
        for (Shot shot : receivedShots) {
            int row = shot.getRow();
            int col = shot.getCol();

            if (shot.getResult() == ShotResult.HIT || shot.getResult() == ShotResult.SUNK) {
                matrix[row][col] = "H";
            } else {
                matrix[row][col] = "M";
            }
        }

        return matrix;
    }

    public String[][] getEnemyBoardView(Game game, User user) {
        User opponent = getOpponent(game, user);
        Board opponentBoard = findByGameAndPlayer(game, opponent);
        int size = opponentBoard.getSize() != null ? opponentBoard.getSize() : 10;

        String[][] matrix = createMatrix(size, "?");

        List<Shot> shotsMadeByUser = shotRepository.findByGameAndShooter(game, user);
        for (Shot shot : shotsMadeByUser) {
            int row = shot.getRow();
            int col = shot.getCol();

            if (shot.getResult() == ShotResult.HIT || shot.getResult() == ShotResult.SUNK) {
                matrix[row][col] = "H";
            } else {
                matrix[row][col] = "M";
            }
        }

        return matrix;
    }

    public boolean hasShipAt(Board targetBoard, int row, int col) {
        return shipPositionRepository.findByBoard(targetBoard)
                .stream()
                .anyMatch(position -> position.getRow().equals(row) && position.getCol().equals(col));
    }

    public long countOccupiedCells(Board board) {
        return shipPositionRepository.findByBoard(board).size();
    }

    private String[][] createMatrix(int size, String defaultValue) {
        String[][] matrix = new String[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = defaultValue;
            }
        }
        return matrix;
    }

    private User getOpponent(Game game, User user) {
        if (game.getPlayer1() != null && game.getPlayer1().getId().equals(user.getId())) {
            return game.getPlayer2();
        }
        if (game.getPlayer2() != null && game.getPlayer2().getId().equals(user.getId())) {
            return game.getPlayer1();
        }
        throw new RuntimeException("El usuario no pertenece a la partida");
    }
}