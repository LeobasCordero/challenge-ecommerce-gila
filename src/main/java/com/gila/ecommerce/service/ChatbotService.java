package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.ChatbotRequestDto;
import com.gila.ecommerce.dto.ChatbotResponseDto;

/**
 * Service interface definition for processing conversational e-commerce requests.
 */
public interface ChatbotService {

    /**
     * Process user conversational prompts to check intents and query analytics metrics.
     * @param request query request payload
     * @param username username context
     * @return response message wrap
     */
    ChatbotResponseDto processQuery(ChatbotRequestDto request, String username);
}
