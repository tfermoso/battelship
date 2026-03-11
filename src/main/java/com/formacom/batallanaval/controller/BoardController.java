package com.formacom.batallanaval.controller;


import com.formacom.batallanaval.dto.ShipPlacementDto;
import com.formacom.batallanaval.model.Board;
import com.formacom.batallanaval.model.Game;
import com.formacom.batallanaval.model.GameStatus;
import com.formacom.batallanaval.model.User;
import com.formacom.batallanaval.service.BoardService;
import com.formacom.batallanaval.service.GameService;
import com.formacom.batallanaval.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/games")
public class BoardController {

    private final GameService gameService;
    private final UserService userService;
    private final BoardService boardService;

    @GetMapping("/{gameId}/setup")
    public String showSetup(@PathVariable Long gameId,
                            Authentication authentication,
                            Model model) {

        Game game = gameService.findById(gameId);
        User user = userService.findByEmail(authentication.getName());

        validatePlayer(game, user);

        if (game.getStatus() == GameStatus.IN_PROGRESS) {
            return "redirect:/games/" + gameId + "/waiting";
        }

        Board board = boardService.getBoardForPlayer(game, user);
        List<Board> boards = boardService.getBoardsForGame(game);

        model.addAttribute("game", game);
        model.addAttribute("board", board);
        model.addAttribute("boards", boards);
        model.addAttribute("placementDto", new ShipPlacementDto());

        return "games/setup";
    }

    @PostMapping("/{gameId}/setup")
    public String saveSetup(@PathVariable Long gameId,
                            @ModelAttribute("placementDto") ShipPlacementDto placementDto,
                            Authentication authentication,
                            Model model) {

        Game game = gameService.findById(gameId);
        User user = userService.findByEmail(authentication.getName());

        validatePlayer(game, user);

        try {
            boardService.saveShipPlacement(game, user, placementDto);
        } catch (RuntimeException e) {
            Board board = boardService.getBoardForPlayer(game, user);
            List<Board> boards = boardService.getBoardsForGame(game);

            model.addAttribute("game", game);
            model.addAttribute("board", board);
            model.addAttribute("boards", boards);
            model.addAttribute("placementDto", placementDto);
            model.addAttribute("error", e.getMessage());

            return "games/setup";
        }

        Game updatedGame = gameService.findById(gameId);

        if (updatedGame.getStatus() == GameStatus.IN_PROGRESS) {
            return "redirect:/games/" + gameId + "/play";
        }

        return "redirect:/games/" + gameId + "/setup";
    }

    private void validatePlayer(Game game, User user) {
        boolean isPlayer1 = game.getPlayer1() != null && game.getPlayer1().getId().equals(user.getId());
        boolean isPlayer2 = game.getPlayer2() != null && game.getPlayer2().getId().equals(user.getId());

        if (!isPlayer1 && !isPlayer2) {
            throw new RuntimeException("No perteneces a esta partida");
        }
    }
}
