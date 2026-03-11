package com.formacom.batallanaval.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formacom.batallanaval.dto.ShipPlacementRequestDto;
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
    private final ObjectMapper objectMapper;

    @GetMapping("/{gameId}/setup")
    public String showSetup(@PathVariable Long gameId,
                            Authentication authentication,
                            Model model) {

        Game game = gameService.findById(gameId);
        User user = userService.findByEmail(authentication.getName());
        validatePlayer(game, user);

        if (game.getStatus() == GameStatus.IN_PROGRESS) {
            return "redirect:/games/" + gameId + "/play";
        }

        Board board = boardService.getBoardForPlayer(game, user);
        List<Board> boards = boardService.getBoardsForGame(game);

        model.addAttribute("game", game);
        model.addAttribute("board", board);
        model.addAttribute("boards", boards);

        return "games/setup";
    }

    @PostMapping("/{gameId}/setup")
    public String saveSetup(@PathVariable Long gameId,
                            @RequestParam("shipsJson") String shipsJson,
                            Authentication authentication,
                            Model model) {

        Game game = gameService.findById(gameId);
        User user = userService.findByEmail(authentication.getName());
        validatePlayer(game, user);

        try {
            ShipPlacementRequestDto dto = objectMapper.readValue(shipsJson, ShipPlacementRequestDto.class);
            boardService.saveShipPlacement(game, user, dto);
        } catch (JsonProcessingException e) {
            model.addAttribute("error", "Formato de barcos no válido");
            reloadModel(game, user, model);
            return "games/setup";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            reloadModel(game, user, model);
            return "games/setup";
        }

        Game updatedGame = gameService.findById(gameId);
        if (updatedGame.getStatus() == GameStatus.IN_PROGRESS) {
            return "redirect:/games/" + gameId + "/play";
        }

        return "redirect:/games/" + gameId + "/setup";
    }

    private void reloadModel(Game game, User user, Model model) {
        Board board = boardService.getBoardForPlayer(game, user);
        List<Board> boards = boardService.getBoardsForGame(game);

        model.addAttribute("game", game);
        model.addAttribute("board", board);
        model.addAttribute("boards", boards);
    }

    private void validatePlayer(Game game, User user) {
        boolean isPlayer1 = game.getPlayer1() != null && game.getPlayer1().getId().equals(user.getId());
        boolean isPlayer2 = game.getPlayer2() != null && game.getPlayer2().getId().equals(user.getId());

        if (!isPlayer1 && !isPlayer2) {
            throw new RuntimeException("No perteneces a esta partida");
        }
    }
}