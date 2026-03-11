package com.formacom.batallanaval.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameController {

    @GetMapping("/games")
    public String games() {
        return "games/list";
    }
}
