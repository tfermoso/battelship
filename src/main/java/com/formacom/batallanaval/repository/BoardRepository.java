package com.formacom.batallanaval.repository;

import com.formacom.batallanaval.model.Board;
import com.formacom.batallanaval.model.Game;
import com.formacom.batallanaval.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Optional<Board> findByGameAndPlayer(Game game, User player);

    List<Board> findByGame(Game game);
}
