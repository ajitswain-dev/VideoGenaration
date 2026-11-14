package com.ajit.springAI.VideoGenaration.service;

import com.ajit.springAI.VideoGenaration.model.AudioArtifact;
import com.ajit.springAI.VideoGenaration.model.Narration;

public interface SpeechGenerator {
    AudioArtifact synthesize(Narration text);
}
