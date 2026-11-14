package com.ajit.springAI.VideoGenaration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ajit.springAI.VideoGenaration.controller.ProjectController;
import com.ajit.springAI.VideoGenaration.model.ContentBrief;
import com.ajit.springAI.VideoGenaration.model.CreateProjectRequest;
import com.ajit.springAI.VideoGenaration.model.ProjectRecord;
import com.ajit.springAI.VideoGenaration.model.Scene;
import com.ajit.springAI.VideoGenaration.model.SceneImprovementRequest;
import com.ajit.springAI.VideoGenaration.model.SceneImprovementResponse;
import com.ajit.springAI.VideoGenaration.model.ScriptDraft;
import com.ajit.springAI.VideoGenaration.service.VideoGenerationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class ProjectControllerTest {

    @Test
    void createProject_shouldAcceptValidRequest() {
        VideoGenerationService service = mock(VideoGenerationService.class);
        ProjectController controller = new ProjectController(service);

        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Launch Plan");
        request.setTopic("AI marketing");
        request.setAudience("founders");
        request.setTone("confident");
        request.setDuration("45s");
        request.setLanguage("English");

        ResponseEntity<ProjectRecord> response = controller.createProject(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Launch Plan", response.getBody().getProjectName());
        assertEquals("AI marketing", response.getBody().getTopic());
    }

    @Test
    void createProject_shouldRejectMissingRequiredValues() {
        VideoGenerationService service = mock(VideoGenerationService.class);
        ProjectController controller = new ProjectController(service);

        CreateProjectRequest request = new CreateProjectRequest();
        request.setTopic("AI marketing");
        request.setAudience("founders");

        assertThrows(ResponseStatusException.class, () -> controller.createProject(request));
    }

    @Test
    void generateDraft_shouldCreateStructuredScript() {
        VideoGenerationService service = mock(VideoGenerationService.class);
        ProjectController controller = new ProjectController(service);

        ProjectRecord project = controller.createProject(newCreateProjectRequest()).getBody();

        ScriptDraft draft = new ScriptDraft();
        draft.setTitle("AI Launch");
        draft.setHook("Start with the problem");
        draft.setNarration("AI helps teams move faster");
        Scene scene = new Scene();
        scene.setSequence(1);
        scene.setVisual("Team reviewing dashboard");
        scene.setNarration("A team sees clarity on metrics");
        scene.setDuration("10s");
        draft.setScenes(List.of(scene));

        when(service.generateScript(any(ContentBrief.class))).thenReturn(draft);

        ContentBrief brief = new ContentBrief();
        brief.setTopic("AI marketing");
        brief.setAudience("founders");
        brief.setTone("confident");
        brief.setDuration("45s");
        brief.setLanguage("English");
        brief.setStyle("cinematic");

        ResponseEntity<ScriptDraft> response = controller.generateDraft(project.getId(), brief);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("AI Launch", response.getBody().getTitle());
        assertEquals("Team reviewing dashboard", response.getBody().getScenes().get(0).getVisual());
    }

    @Test
    void getProjectVersions_shouldReturnHistory() {
        VideoGenerationService service = mock(VideoGenerationService.class);
        ProjectController controller = new ProjectController(service);

        ProjectRecord project = controller.createProject(newCreateProjectRequest()).getBody();
        ResponseEntity<List<ScriptDraft>> response = controller.getVersions(project.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void improveScene_shouldReturnImprovedText() {
        VideoGenerationService service = mock(VideoGenerationService.class);
        ProjectController controller = new ProjectController(service);

        ProjectRecord project = controller.createProject(newCreateProjectRequest()).getBody();

        ScriptDraft draft = new ScriptDraft();
        draft.setTitle("AI Launch");
        Scene scene = new Scene();
        scene.setSequence(1);
        scene.setNarration("This is a basic scene.");
        draft.setScenes(List.of(scene));
        project.setCurrentDraft(draft);

        when(service.improveScene(any(ScriptDraft.class), any(Integer.class), any(String.class), any(String.class)))
                .thenReturn("This scene is more energetic and punchy.");

        SceneImprovementRequest request = new SceneImprovementRequest();
        request.setSceneIndex(0);
        request.setSceneText("This is a basic scene.");
        request.setImprovementRequest("Make it more persuasive");

        ResponseEntity<SceneImprovementResponse> response = controller.improveScene(project.getId(), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("This scene is more energetic and punchy.", response.getBody().getImprovedText());
    }

    private CreateProjectRequest newCreateProjectRequest() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Launch Plan");
        request.setTopic("AI marketing");
        request.setAudience("founders");
        request.setTone("confident");
        request.setDuration("45s");
        request.setLanguage("English");
        return request;
    }
}
