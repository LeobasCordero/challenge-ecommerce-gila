package com.gila.ecommerce.controller;

import com.gila.ecommerce.api.ChatbotApi;
import com.gila.ecommerce.dto.ChatbotRequestDto;
import com.gila.ecommerce.dto.ChatbotResponseDto;
import com.gila.ecommerce.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing conversational assistant endpoints.
 */
@RestController
public class ChatbotController implements ChatbotApi {

    private final ChatbotService chatbotService;

    /**
     * Constructor injecting ChatbotService.
     * @param chatbotService conversational service manager
     */
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @Override
    public ResponseEntity<ChatbotResponseDto> queryChatbot(ChatbotRequestDto chatbotRequestDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ChatbotResponseDto response = chatbotService.processQuery(chatbotRequestDto, username);

        // If response contains an error block (like injection attempts), return 400 Bad Request
        if (response.getReply() != null && response.getReply().startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
