package com.ajit.springAI.VideoGenaration.service;

import com.ajit.springAI.VideoGenaration.model.GeneratedImage;
import com.ajit.springAI.VideoGenaration.model.PanelPrompt;

public interface ImageGenerator {
    GeneratedImage generate(PanelPrompt prompt);
}
