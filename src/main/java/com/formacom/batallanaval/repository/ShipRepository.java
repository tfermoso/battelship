package com.formacom.batallanaval.repository;

import com.formacom.batallanaval.model.Board;
import com.formacom.batallanaval.model.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipRepository extends JpaRepository<Ship, Long> {
    List<Ship> findByBoard(Board board);
}