package com.example.triagecopilot.controller;

import com.example.triagecopilot.common.ApiResponse;
import com.example.triagecopilot.dto.ChatRequest;
import com.example.triagecopilot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ApiResponse<String> send(@Valid @RequestBody ChatRequest request) {
        try {
            return ApiResponse.success(chatService.chat(request));
        } catch (Throwable ex) {
            return ApiResponse.fail("Chat processing failed: " + ex.getMessage());
        }
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("ok");
    }
}
