package com.ajit.springAI.VideoGenaration.service;

import com.ajit.springAI.VideoGenaration.model.AudioArtifact;
import com.ajit.springAI.VideoGenaration.model.Narration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CaptionSpeechGenerator implements SpeechGenerator {

    private final Path baseDirectory;

    public CaptionSpeechGenerator() {
        this(Paths.get("generated", "captions"));
    }

    public CaptionSpeechGenerator(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public AudioArtifact synthesize(Narration narration) {
        String text = narration != null && narration.getText() != null ? narration.getText() : "Narration";
        try {
            Files.createDirectories(baseDirectory);
            Path captionFile = baseDirectory.resolve("caption-" + Math.abs(text.hashCode()) + ".txt");
            Files.writeString(captionFile, text, StandardCharsets.UTF_8);

            AudioArtifact artifact = new AudioArtifact();
            artifact.setAudioUrl("");
            artifact.setCaptionUrl(captionFile.toString());
            artifact.setProvider("local-caption");
            artifact.setModel("caption-only");
            artifact.setStatus("COMPLETED");
            return artifact;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate captions", e);
        }
    }
}
