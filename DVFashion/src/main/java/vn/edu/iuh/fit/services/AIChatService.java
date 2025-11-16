/*
 * @ {#} AIChatService.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * @description: Service interface for AI chat functionalities
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */

public interface AIChatService {
    /**
     * Sends a message to the AI service and retrieves the response.
     *
     * @param message The message to send to the AI.
     * @return The AI's response as a JsonNode.
     */
    JsonNode sendToAI(String message);
}
