package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.ChatbotRequestDto;
import com.gila.ecommerce.dto.ChatbotResponseDto;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.repository.OrderItemRepository;
import com.gila.ecommerce.repository.OrderRepository;
import com.gila.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Service implementation managing multi-agent chatbot simulation querying catalog metrics.
 */
@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    // Prompt injection blacklist keywords
    private static final List<String> INJECTION_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "system", "ignore", "override", "act as", "role:", "sql", "drop table", "delete from", "select *"
    ));

    // Product domain whitelist keywords
    private static final List<String> DOMAIN_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
            "product", "item", "catalog", "stock", "inventory", "price", "sell", "sold", "sales", "revenue", 
            "most", "lowest", "highest", "count", "today", "volume", "popular"
    ));

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Value("${chatbot.llm-provider:gemini}")
    private String llmProvider;

    /**
     * Constructor injecting repositories.
     * @param productRepository catalog repository
     * @param orderRepository order transaction repository
     * @param orderItemRepository order line items repository
     */
    public ChatbotServiceImpl(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public ChatbotResponseDto processQuery(ChatbotRequestDto request, String username) {
        logger.info("Chatbot query received from user '{}' using LLM provider '{}'", username, llmProvider);

        String rawMessage = request.getMessage();
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            ChatbotResponseDto response = new ChatbotResponseDto();
            response.setReply("Error: Query message cannot be empty.");
            return response;
        }

        String lowerMessage = rawMessage.toLowerCase(Locale.ROOT);

        // 1. Prompt Injection Guard (Highest Priority)
        for (String injectionKeyword : INJECTION_KEYWORDS) {
            if (lowerMessage.contains(injectionKeyword)) {
                logger.warn("Prompt injection attempt blocked from user '{}': '{}'", username, rawMessage);
                ChatbotResponseDto response = new ChatbotResponseDto();
                response.setReply("Error: System instruction override attempt detected. Action blocked.");
                return response;
            }
        }

        // 2. Product Domain Verification Gate
        boolean hasDomainKeyword = false;
        for (String domainKeyword : DOMAIN_KEYWORDS) {
            if (lowerMessage.contains(domainKeyword)) {
                hasDomainKeyword = true;
                break;
            }
        }

        if (!hasDomainKeyword) {
            logger.info("Query by '{}' blocked as out-of-domain: '{}'", username, rawMessage);
            ChatbotResponseDto response = new ChatbotResponseDto();
            response.setReply("I am an e-commerce assistant and can only help with questions regarding products and store catalog statistics.");
            return response;
        }

        // 3. Multi-Agent Simulation: Intent Router -> Data Fetcher -> Response Formatter
        String agentReply = executeAgentsWorkflow(lowerMessage);

        // Append LLM signature footer
        String formattedReply = agentReply + "\n\n[Formatted via " + llmProvider.substring(0, 1).toUpperCase(Locale.ROOT) + llmProvider.substring(1).toLowerCase(Locale.ROOT) + "]";
        
        ChatbotResponseDto response = new ChatbotResponseDto();
        response.setReply(formattedReply);
        return response;
    }

    private String executeAgentsWorkflow(String lowerMessage) {
        // Intent Router Agent
        if (lowerMessage.contains("most sold") || lowerMessage.contains("top selling") || lowerMessage.contains("best seller") || lowerMessage.contains("popular")) {
            // Data Fetcher Agent: Top Selling Product
            List<Product> topProducts = orderItemRepository.findTopSellingProducts(PageRequest.of(0, 1));
            if (topProducts.isEmpty()) {
                return "There are no checkout order transactions recorded yet to calculate sales volume statistics.";
            }
            Product top = topProducts.get(0);
            return "The most sold product in the store is **" + top.getName() + "** (Price: $" + top.getPrice() + ", Current Stock: " + top.getStock() + " units).";
        }

        if (lowerMessage.contains("sales volume") || lowerMessage.contains("revenue") || lowerMessage.contains("sales today") || lowerMessage.contains("volume today") || lowerMessage.contains("today's sales")) {
            // Data Fetcher Agent: Today's Sales Volume & Revenue
            OffsetDateTime startOfToday = OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC);
            BigDecimal revenueToday = orderRepository.calculateRevenueSince(startOfToday);
            long ordersToday = orderRepository.countOrdersSince(startOfToday);
            return "Today's sales statistics show **" + ordersToday + "** transaction(s) generating total revenue of **$" + revenueToday + "**.";
        }

        if (lowerMessage.contains("low stock") || lowerMessage.contains("out of stock") || lowerMessage.contains("inventory alert")) {
            // Data Fetcher Agent: Low Stock Alert (limit stock < 5)
            List<Product> lowStock = productRepository.findLowStockProducts(5);
            if (lowStock.isEmpty()) {
                return "Great news! All products in the catalog have sufficient stock levels (no products are under 5 units).";
            }
            String productList = lowStock.stream()
                    .map(p -> "- " + p.getName() + " (Stock: **" + p.getStock() + "** left)")
                    .collect(Collectors.joining("\n"));
            return "Here are the products currently low in stock (below 5 units):\n" + productList;
        }

        if (lowerMessage.contains("count") || lowerMessage.contains("how many products") || lowerMessage.contains("catalog size")) {
            // Data Fetcher Agent: Total Products
            long totalProducts = productRepository.count();
            return "There are currently **" + totalProducts + "** distinct product(s) registered in the store catalog.";
        }

        // Generic fallback help query
        return "I can help you query store statistics. Try asking things like: \n" +
               "- *\"What is the most sold product?\"*\n" +
               "- *\"What's the sales volume today?\"*\n" +
               "- *\"Which products are low in stock?\"*";
    }
}
