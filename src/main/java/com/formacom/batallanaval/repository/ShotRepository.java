package com.formacom.batallanaval.repository;

import com.formacom.batallanaval.model.Board;
import com.formacom.batallanaval.model.Game;
import com.formacom.batallanaval.model.Shot;
import com.formacom.batallanaval.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShotRepository extends JpaRepository<Shot, Long> {

    List<Shot> findByGameOrderByCreatedAtAsc(Game game);

    List<Shot> findByGameAndShooter(Game game, User shooter);

    List<Shot> findByGameAndTargetBoard(Game game, Board targetBoard);

    long countByGame(Game game);

    boolean existsByGameAndShooterAndRowAndCol(Game game, User shooter, Integer row, Integer col);
}