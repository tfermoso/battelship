package com.formacom.batallanaval.service;


import com.formacom.batallanaval.dto.ShipPlacementDto;
import com.formacom.batallanaval.model.*;
import com.formacom.batallanaval.repository.BoardRepository;
import com.formacom.batallanaval.repository.ShipPositionRepository;
import com.formacom.batallanaval.repository.ShipRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final ShipRepository shipRepository;
    private final ShipPositionRepository shipPositionRepository;
    private final GameService gameService;

    public Board getBoardForPlayer(Game game, User player) {
        return boardRepository.findByGameAndPlayer(game, player)
                .orElseThrow(() -> new RuntimeException("Tablero no encontrado"));
    }

    public List<Board> getBoardsForGame(Game game) {
        return boardRepository.findByGame(game);
    }

    @Transactional
    public void saveShipPlacement(Game game, User player, ShipPlacementDto dto) {
        Board board = getBoardForPlayer(game, player);

        validatePlacement(board, dto);

        clearBoard(board);
        createShips(board, dto);

        board.setReady(true);
        boardRepository.save(board);

        checkIfBothBoardsAreReady(game);
    }

    private void validatePlacement(Board board, ShipPlacementDto dto) {
        Set<String> occupied = new HashSet<>();
        int boardSize = board.getSize();

        addShipCells(occupied, dto.getLargeRow(), dto.getLargeCol(), dto.getLargeOrientation(), 4, boardSize);
        addShipCells(occupied, dto.getMedium1Row(), dto.getMedium1Col(), dto.getMedium1Orientation(), 2, boardSize);
        addShipCells(occupied, dto.getMedium2Row(), dto.getMedium2Col(), dto.getMedium2Orientation(), 2, boardSize);

        addShipCells(occupied, dto.getSmall1Row(), dto.getSmall1Col(), null, 1, boardSize);
        addShipCells(occupied, dto.getSmall2Row(), dto.getSmall2Col(), null, 1, boardSize);
        addShipCells(occupied, dto.getSmall3Row(), dto.getSmall3Col(), null, 1, boardSize);
    }

    private void addShipCells(Set<String> occupied,
                              Integer row,
                              Integer col,
                              String orientationText,
                              int size,
                              int boardSize) {
        if (row == null || col == null) {
            throw new RuntimeException("Todas las posiciones de barcos son obligatorias");
        }

        Orientation orientation = null;
        if (size > 1) {
            if (orientationText == null || orientationText.isBlank()) {
                throw new RuntimeException("La orientación es obligatoria para barcos de tamaño mayor que 1");
            }
            orientation = Orientation.valueOf(orientationText);
        }

        for (int i = 0; i < size; i++) {
            int currentRow = row;
            int currentCol = col;

            if (size > 1) {
                if (orientation == Orientation.HORIZONTAL) {
                    currentCol = col + i;
                } else {
                    currentRow = row + i;
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

    private void clearBoard(Board board) {
        List<Ship> ships = shipRepository.findByBoard(board);
        shipPositionRepository.deleteByBoard(board);
        shipRepository.deleteAll(ships);
        board.setReady(false);
        boardRepository.save(board);
    }

    private void createShips(Board board, ShipPlacementDto dto) {
        createShip(board, ShipType.LARGE, 4, dto.getLargeRow(), dto.getLargeCol(), dto.getLargeOrientation());
        createShip(board, ShipType.MEDIUM, 2, dto.getMedium1Row(), dto.getMedium1Col(), dto.getMedium1Orientation());
        createShip(board, ShipType.MEDIUM, 2, dto.getMedium2Row(), dto.getMedium2Col(), dto.getMedium2Orientation());

        createShip(board, ShipType.SMALL, 1, dto.getSmall1Row(), dto.getSmall1Col(), null);
        createShip(board, ShipType.SMALL, 1, dto.getSmall2Row(), dto.getSmall2Col(), null);
        createShip(board, ShipType.SMALL, 1, dto.getSmall3Row(), dto.getSmall3Col(), null);
    }

    private void createShip(Board board,
                            ShipType type,
                            int size,
                            Integer row,
                            Integer col,
                            String orientationText) {

        Ship ship = Ship.builder()
                .type(type)
                .size(size)
                .sunk(false)
                .board(board)
                .build();

        ship = shipRepository.save(ship);

        Orientation orientation = null;
        if (size > 1) {
            orientation = Orientation.valueOf(orientationText);
        }

        for (int i = 0; i < size; i++) {
            int currentRow = row;
            int currentCol = col;

            if (size > 1) {
                if (orientation == Orientation.HORIZONTAL) {
                    currentCol = col + i;
                } else {
                    currentRow = row + i;
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

    private void checkIfBothBoardsAreReady(Game game) {
        List<Board> boards = boardRepository.findByGame(game);

        boolean allReady = boards.size() == 2 &&
                boards.stream().allMatch(board -> Boolean.TRUE.equals(board.getReady()));

        if (allReady) {
            gameService.markGameAsInProgress(game);
        }
    }
}
