package com.ajit.springAI.VideoGenaration.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ajit.springAI.VideoGenaration.service.VideoGenerationService;

@RestController
@RequestMapping("/api")
public class videoController {

    private final VideoGenerationService videoGenerationService;

    public videoController(VideoGenerationService videoGenerationService) {
        this.videoGenerationService = videoGenerationService;
    }

    @GetMapping("/chat")
    public Map<String, String> chat(@RequestParam String message) {
        String response = videoGenerationService.generateText(message);
        return Map.of("message", message, "response", response);
    }
}
