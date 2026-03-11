package com.formacom.batallanaval.controller;


import com.formacom.batallanaval.dto.RegisterUserDto;
import com.formacom.batallanaval.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerUserDto", new RegisterUserDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerUserDto") RegisterUserDto registerUserDto,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(registerUserDto);
        } catch (RuntimeException e) {
            result.rejectValue("email", "error.registerUserDto", e.getMessage());
            return "auth/register";
        }

        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}
