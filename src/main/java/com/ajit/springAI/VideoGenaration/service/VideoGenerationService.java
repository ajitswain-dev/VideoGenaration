package com.ajit.springAI.VideoGenaration.service;

import com.ajit.springAI.VideoGenaration.model.ContentBrief;
import com.ajit.springAI.VideoGenaration.model.RevisionRequest;
import com.ajit.springAI.VideoGenaration.model.Scene;
import com.ajit.springAI.VideoGenaration.model.ScriptDraft;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class VideoGenerationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VideoGenerationService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generateText(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    public ScriptDraft generateScript(ContentBrief brief) {
        String prompt = buildScriptPrompt(brief);
        String content = generateText(prompt);
        try {
            String cleanedJson = extractJson(content);
            return objectMapper.readValue(cleanedJson, ScriptDraft.class);
        } catch (JsonProcessingException e) {
            ScriptDraft fallback = new ScriptDraft();
            fallback.setTitle("Video Script");
            fallback.setHook("A strong opening statement for your topic.");
            fallback.setNarration(brief.getTopic());
            fallback.setStatus("generated");
            fallback.setLanguage(brief.getLanguage());
            List<Scene> scenes = new ArrayList<>();
            Scene scene = new Scene();
            scene.setSequence(1);
            scene.setVisual(brief.getTopic());
            scene.setNarration(brief.getTopic());
            scene.setDuration(brief.getDuration());
            scenes.add(scene);
            fallback.setScenes(scenes);
            return fallback;
        }
    }

    public ScriptDraft reviseScript(ScriptDraft current, RevisionRequest revisionRequest) {
        String prompt = "You are revising a video script. Keep the structure, improve it using the feedback, and return strict JSON with title, hook, narration, status, language, and scenes[] where each scene has sequence, visual, narration and duration. Current script: "
                + objectToJson(current)
                + "\nUser feedback: " + revisionRequest.getFeedback();

        String content = generateText(prompt);
        try {
            String cleanedJson = extractJson(content);
            ScriptDraft revised = objectMapper.readValue(cleanedJson, ScriptDraft.class);
            revised.setStatus("revised");
            return revised;
        } catch (JsonProcessingException e) {
            ScriptDraft fallback = copyCurrentWithStatus(current, "revised");
            fallback.setNarration(revisionRequest.getFeedback());
            return fallback;
        }
    }

    public String improveScene(ScriptDraft currentDraft, int sceneIndex, String sceneText, String improvementRequest) {
        if (currentDraft == null || sceneText == null || sceneText.isBlank()) {
            return "";
        }

        String prompt = "Improve this specific video scene. Keep the meaning, make it more polished, and follow this request: "
                + improvementRequest
                + ". Return only the improved scene text. Scene text:\n"
                + sceneText;

        String improvement = generateText(prompt);
        return improvement == null ? sceneText : improvement.trim();
    }

    private String buildScriptPrompt(ContentBrief brief) {
        return "Create a short marketing video script in JSON only. "
                + "Fields: title, hook, narration, status, language, scenes[] where each scene has sequence, visual, narration, duration. "
                + "Topic: " + brief.getTopic() + ". Audience: " + brief.getAudience() + ". Tone: " + brief.getTone()
                + ". Duration: " + brief.getDuration() + ". Language: " + brief.getLanguage()
                + ". Style: " + (brief.getStyle() == null ? "cinematic" : brief.getStyle())
                + ". Return valid JSON without markdown fences.";
    }

    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        String cleaned = content.trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private String objectToJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return value == null ? "{}" : value.toString();
        }
    }

    private ScriptDraft copyCurrentWithStatus(ScriptDraft current, String status) {
        ScriptDraft copy = new ScriptDraft();
        copy.setTitle(current.getTitle());
        copy.setHook(current.getHook());
        copy.setNarration(current.getNarration());
        copy.setScenes(current.getScenes() == null ? new ArrayList<>() : current.getScenes());
        copy.setStatus(status);
        copy.setLanguage(current.getLanguage());
        return copy;
    }
}
