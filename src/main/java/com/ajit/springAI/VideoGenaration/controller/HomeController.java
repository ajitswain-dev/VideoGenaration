package com.ajit.springAI.VideoGenaration.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/editor.html";
    }

    @GetMapping("/editor")
    public String editor() {
        return "redirect:/editor.html";
    }
}
