package com.formacom.batallanaval.controller;

import com.formacom.batallanaval.dto.CreateGameDto;
import com.formacom.batallanaval.model.Game;
import com.formacom.batallanaval.model.GameStatus;
import com.formacom.batallanaval.model.User;
import com.formacom.batallanaval.service.GameService;
import com.formacom.batallanaval.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;

    @GetMapping
    public String listGames(Model model) {
        List<Game> games = gameService.findAvailableGames();
        model.addAttribute("games", games);
        return "games/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("createGameDto", new CreateGameDto());
        return "games/create";
    }

    @PostMapping("/create")
    public String createGame(@Valid @ModelAttribute CreateGameDto createGameDto,
                             BindingResult result,
                             Authentication authentication) {
        if (result.hasErrors()) {
            return "games/create";
        }

        User user = userService.findByEmail(authentication.getName());
        Game game = gameService.createGame(createGameDto, user);

        return "redirect:/games/" + game.getId() + "/waiting";
    }

    @PostMapping("/{id}/join")
    public String joinGame(@PathVariable Long id,
                           Authentication authentication,
                           Model model) {
        User user = userService.findByEmail(authentication.getName());

        try {
            Game game = gameService.joinGame(id, user);
            return "redirect:/games/" + game.getId() + "/waiting";
        } catch (RuntimeException e) {
            model.addAttribute("games", gameService.findAvailableGames());
            model.addAttribute("error", e.getMessage());
            return "games/list";
        }
    }

    @GetMapping("/{id}/waiting")
    public String waitingRoom(@PathVariable Long id,
                              Authentication authentication,
                              Model model) {
        Game game = gameService.findById(id);
        User user = userService.findByEmail(authentication.getName());

        boolean isPlayer1 = game.getPlayer1() != null && game.getPlayer1().getId().equals(user.getId());
        boolean isPlayer2 = game.getPlayer2() != null && game.getPlayer2().getId().equals(user.getId());

        if (!isPlayer1 && !isPlayer2) {
            return "redirect:/games";
        }
        if (game.getStatus() == GameStatus.PREPARING) {
            return "redirect:/games/" + id + "/setup";
        }
        if (game.getStatus() == GameStatus.IN_PROGRESS) {
            return "redirect:/games/" + id + "/play";
        }

        model.addAttribute("game", game);
        model.addAttribute("isPlayer1", isPlayer1);
        model.addAttribute("isPlayer2", isPlayer2);

        return "games/waiting";
    }
    @GetMapping("/{id}/play")
    public String playPlaceholder(@PathVariable Long id,
                                  Authentication authentication,
                                  Model model) {
        Game game = gameService.findById(id);
        User user = userService.findByEmail(authentication.getName());

        boolean isPlayer1 = game.getPlayer1() != null && game.getPlayer1().getId().equals(user.getId());
        boolean isPlayer2 = game.getPlayer2() != null && game.getPlayer2().getId().equals(user.getId());

        if (!isPlayer1 && !isPlayer2) {
            return "redirect:/games";
        }

        model.addAttribute("game", game);
        return "games/play";
    }
}