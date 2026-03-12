package com.formacom.batallanaval.controller;

import com.formacom.batallanaval.dto.CreateGameDto;
import com.formacom.batallanaval.model.Game;
import com.formacom.batallanaval.model.GameStatus;
import com.formacom.batallanaval.model.User;
import com.formacom.batallanaval.service.BoardService;
import com.formacom.batallanaval.service.GameService;
import com.formacom.batallanaval.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;
    private final BoardService boardService;

    @GetMapping
    public String listGames(Model model, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());

        List<Game> availableGames = gameService.findJoinableGames(user);
        List<Game> preparingGames = gameService.findGamesForUserByStatus(user, GameStatus.PREPARING);
        List<Game> inProgressGames = gameService.findGamesForUserByStatus(user, GameStatus.IN_PROGRESS);
        List<Game> finishedGames = gameService.findGamesForUserByStatus(user, GameStatus.FINISHED);

        model.addAttribute("availableGames", availableGames);
        model.addAttribute("preparingGames", preparingGames);
        model.addAttribute("inProgressGames", inProgressGames);
        model.addAttribute("finishedGames", finishedGames);

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
    public String play(@PathVariable Long id,
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

        User opponent = gameService.getOpponent(game, user);

        model.addAttribute("game", game);
        model.addAttribute("currentUser", user);
        model.addAttribute("opponent", opponent);
        model.addAttribute("isMyTurn", gameService.isUsersTurn(game, user));
        model.addAttribute("shotDto", new com.formacom.batallanaval.dto.ShotDto());
        model.addAttribute("myBoard", boardService.getBoardMatrix(game, user));
        model.addAttribute("enemyBoard", boardService.getEnemyBoardView(game, user));
        model.addAttribute("winner", gameService.getWinner(game));

        return "games/play";
    }

    @PostMapping("/{id}/play/shoot")
    public String shoot(@PathVariable Long id,
                        @Valid @ModelAttribute("shotDto") com.formacom.batallanaval.dto.ShotDto shotDto,
                        BindingResult result,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        Game game = gameService.findById(id);
        User user = userService.findByEmail(authentication.getName());

        boolean isPlayer1 = game.getPlayer1() != null && game.getPlayer1().getId().equals(user.getId());
        boolean isPlayer2 = game.getPlayer2() != null && game.getPlayer2().getId().equals(user.getId());

        if (!isPlayer1 && !isPlayer2) {
            return "redirect:/games";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Coordenadas no válidas");
            return "redirect:/games/" + id + "/play";
        }

        try {
            gameService.shoot(game, user, shotDto.getX(), shotDto.getY());
            redirectAttributes.addFlashAttribute("success", "Disparo realizado");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/games/" + id + "/play";
    }
}