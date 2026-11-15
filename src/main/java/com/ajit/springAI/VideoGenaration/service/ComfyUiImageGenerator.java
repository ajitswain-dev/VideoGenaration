package com.ajit.springAI.VideoGenaration.service;

import com.ajit.springAI.VideoGenaration.model.GeneratedImage;
import com.ajit.springAI.VideoGenaration.model.PanelPrompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Primary
public class ComfyUiImageGenerator implements ImageGenerator {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TemplateImageGenerator fallbackGenerator = new TemplateImageGenerator();
    private final String baseUrl;

    public ComfyUiImageGenerator(@Value("${comfyui.base-url:http://localhost:8188}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public GeneratedImage generate(PanelPrompt prompt) {
        try {
            String sceneText = prompt != null && prompt.getScene() != null && !prompt.getScene().isBlank()
                    ? prompt.getScene()
                    : "Comic panel";

            String promptId = submitPrompt(sceneText);
            if (promptId == null || promptId.isBlank()) {
                return fallbackGenerator.generate(prompt);
            }

            String imageFilename = waitForImage(promptId);
            if (imageFilename == null || imageFilename.isBlank()) {
                return fallbackGenerator.generate(prompt);
            }

            byte[] imageBytes = fetchImage(imageFilename);
            if (imageBytes == null || imageBytes.length == 0) {
                return fallbackGenerator.generate(prompt);
            }

            Path outputDir = Path.of("generated", "comfyui");
            Files.createDirectories(outputDir);
            String safeName = sanitize(sceneText);
            Path outputFile = outputDir.resolve(safeName + "-comfyui.png");
            Files.write(outputFile, imageBytes);
            return new GeneratedImage(sceneText, outputFile.toString());
        } catch (Exception e) {
            return fallbackGenerator.generate(prompt);
        }
    }

    private String submitPrompt(String sceneText) {
        Map<String, Object> workflow = new HashMap<>();
        workflow.put("prompt", buildWorkflow(sceneText));
        workflow.put("client_id", "video-generation-demo");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(workflow);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/prompt",
                HttpMethod.POST,
                request,
                Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return null;
        }

        Object promptId = response.getBody().get("prompt_id");
        return promptId == null ? null : promptId.toString();
    }

    private String waitForImage(String promptId) {
        for (int attempt = 0; attempt < 30; attempt++) {
            try {
                String url = baseUrl + "/history/" + URLEncoder.encode(promptId, StandardCharsets.UTF_8);
                Map<String, Object> history = restTemplate.getForObject(url, Map.class);
                if (history == null || !history.containsKey(promptId)) {
                    Thread.sleep(1000L);
                    continue;
                }

                Map<String, Object> item = (Map<String, Object>) history.get(promptId);
                if (item == null || !item.containsKey("outputs")) {
                    Thread.sleep(1000L);
                    continue;
                }

                Map<String, Object> outputs = (Map<String, Object>) item.get("outputs");
                if (outputs == null || outputs.isEmpty()) {
                    Thread.sleep(1000L);
                    continue;
                }

                for (Object outputValue : outputs.values()) {
                    Map<String, Object> output = (Map<String, Object>) outputValue;
                    if (output == null) {
                        continue;
                    }
                    Object imageList = output.get("images");
                    if (!(imageList instanceof List<?> list) || list.isEmpty()) {
                        continue;
                    }
                    Object first = list.get(0);
                    if (first instanceof Map<?, ?> imageEntry) {
                        Object filename = imageEntry.get("filename");
                        if (filename != null) {
                            return filename.toString();
                        }
                    }
                }
            } catch (Exception ignored) {
                // fallback to next polling round
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }

    private byte[] fetchImage(String filename) {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        String url = baseUrl + "/view?filename=" + encodedFilename + "&type=output&subfolder=";
        return restTemplate.getForObject(url, byte[].class);
    }

    private Map<String, Object> buildWorkflow(String sceneText) {
        Map<String, Object> prompt = new HashMap<>();

        Map<String, Object> clipTextInputs = new HashMap<>();
        clipTextInputs.put("text", sceneText);
        clipTextInputs.put("clip", Map.of());

        Map<String, Object> checkpointInputs = new HashMap<>();
        checkpointInputs.put("ckpt_name", "sd_xl_base_1.0.safetensors");

        Map<String, Object> emptyLatentInputs = new HashMap<>();
        emptyLatentInputs.put("width", 1024);
        emptyLatentInputs.put("height", 576);
        emptyLatentInputs.put("batch_size", 1);

        Map<String, Object> kSamplerInputs = new HashMap<>();
        kSamplerInputs.put("seed", 42);
        kSamplerInputs.put("steps", 20);
        kSamplerInputs.put("cfg", 7.0);
        kSamplerInputs.put("sampler_name", "euler");
        kSamplerInputs.put("scheduler", "normal");
        kSamplerInputs.put("denoise", 1.0);

        Map<String, Object> vaeDecodeInputs = new HashMap<>();
        Map<String, Object> saveImageInputs = new HashMap<>();

        prompt.put("1", Map.of("inputs", clipTextInputs, "class_type", "CLIPTextEncode"));
        prompt.put("2", Map.of("inputs", checkpointInputs, "class_type", "CheckpointLoaderSimple"));
        prompt.put("3", Map.of("inputs", emptyLatentInputs, "class_type", "EmptyLatentImage"));
        prompt.put("4", Map.of("inputs", kSamplerInputs, "class_type", "KSampler"));
        prompt.put("5", Map.of("inputs", vaeDecodeInputs, "class_type", "VAEDecode"));
        prompt.put("6", Map.of("inputs", saveImageInputs, "class_type", "SaveImage"));

        Map<String, Object> kSamplerRef = new HashMap<>();
        kSamplerRef.put("model", List.of("2", 0));
        kSamplerRef.put("positive", List.of("1", 0));
        kSamplerRef.put("negative", List.of("1", 0));
        kSamplerRef.put("latent_image", List.of("3", 0));
        prompt.replace("4", Map.of("inputs", kSamplerRef, "class_type", "KSampler"));

        Map<String, Object> vaeDecodeRef = new HashMap<>();
        vaeDecodeRef.put("samples", List.of("4", 0));
        vaeDecodeRef.put("vae", List.of("2", 2));
        prompt.replace("5", Map.of("inputs", vaeDecodeRef, "class_type", "VAEDecode"));

        Map<String, Object> saveImageRef = new HashMap<>();
        saveImageRef.put("images", List.of("5", 0));
        prompt.replace("6", Map.of("inputs", saveImageRef, "class_type", "SaveImage"));

        return prompt;
    }

    private String sanitize(String text) {
        if (text == null || text.isBlank()) {
            return "panel";
        }
        return text.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
