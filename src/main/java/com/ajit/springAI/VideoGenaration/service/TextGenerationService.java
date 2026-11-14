package com.ajit.springAI.VideoGenaration.service;

import com.ajit.springAI.VideoGenaration.model.ContentBrief;
import com.ajit.springAI.VideoGenaration.model.RevisionRequest;
import com.ajit.springAI.VideoGenaration.model.ScriptDraft;

public interface TextGenerationService {
    ScriptDraft createDraft(ContentBrief brief);

    ScriptDraft revise(ScriptDraft current, RevisionRequest feedback);
}
