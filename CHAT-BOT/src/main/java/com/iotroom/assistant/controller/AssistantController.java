package com.iotroom.assistant.controller;

import com.iotroom.assistant.dto.AssistantChatRequest;
import com.iotroom.assistant.dto.AssistantChatResponse;
import com.iotroom.assistant.dto.AssistantFeedbackRequest;
import com.iotroom.assistant.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = {"/api/assistente", "/api/assistant"})
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AssistantChatResponse> chat(
            @Valid @RequestBody AssistantChatRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(assistantService.chat(request, authentication));
    }

    @PostMapping("/feedback")
    public ResponseEntity<Void> feedback(
            @Valid @RequestBody AssistantFeedbackRequest request
    ) {
        assistantService.feedback(request);
        return ResponseEntity.ok().build();
    }
}