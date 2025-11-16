/*
 * @ {#} AIChatServiceImpl.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.services.AIChatService;

import java.util.Map;

/*
 * @description: Implementation of AIChatService to interact with AI chat functionalities
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatServiceImpl implements AIChatService {
    @Value("#{ '${recommendation.service.url}' + '${web.base-path}' }")
    private String recommendationServiceUrl;

    private final RestTemplate restTemplate;

    private final ObjectMapper mapper;

    @Override
    public JsonNode sendToAI(String message) {
        String apiUrl = recommendationServiceUrl + "/chat";
        try {
            String body = mapper.writeValueAsString(Map.of("message", message));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            return mapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Error calling AI: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable");
        }
    }
}
