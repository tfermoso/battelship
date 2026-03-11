package com.formacom.batallanaval.repository;

import com.formacom.batallanaval.model.Board;
import com.formacom.batallanaval.model.ShipPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipPositionRepository extends JpaRepository<ShipPosition, Long> {

    List<ShipPosition> findByBoard(Board board);

    boolean existsByBoardAndRowAndCol(Board board, Integer row, Integer col);

    long deleteByBoard(Board board);
}