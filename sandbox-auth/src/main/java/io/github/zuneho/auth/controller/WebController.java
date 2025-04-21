package io.github.zuneho.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class WebController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/oauth2/redirect")
    public ModelAndView oauth2Redirect(@RequestParam("token") String token) {
        ModelAndView modelAndView = new ModelAndView("oauth2/redirect");
        modelAndView.addObject("token", token);
        return modelAndView;
    }
}
