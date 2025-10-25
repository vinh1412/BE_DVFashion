/*
 * @ {#} ContentModerationServiceImpl.java   1.0     18/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

/*
 * @description: Implementation of the ContentModerationService using AI for moderating text and images.
 * @author: Tran Hien Vinh
 * @date:   18/10/2025
 * @version:    1.0
 */

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.ContentModerationResult;
import vn.edu.iuh.fit.services.ContentModerationService;

@Service
public class ContentModerationServiceImpl implements ContentModerationService {
    private final ChatClient chatClient;

    public ContentModerationServiceImpl(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    @Override
    public ContentModerationResult moderateText(String content) {
        // If content is empty, return not violated
        if (content == null || content.trim().isEmpty()) {
            return new ContentModerationResult(false, "", 1.0);
        }

        // Build prompt for AI moderation
        /*
         * Censorship rules(Các quy tắc kiểm duyệt):
         * Hate speech / Discrimination / Offensive language | Ngôn từ thù ghét, phân biệt chủng tộc, xúc phạm
         * Personal attacks / Harassment                     | Công kích cá nhân, quấy rối
         * Spam / Promotional content                        | Quảng cáo, spam
         * False / Misleading information                    | Thông tin sai lệch, giả mạo
         * Inappropriate / Explicit content                  | Nội dung phản cảm, đồi trụy
         * Off-topic / Irrelevant content                    | Không liên quan đến chủ đề đánh giá
         */
        String prompt = String.format("""
            You are a content moderation AI. Analyze the following review comment and determine if it violates any of these rules: 
            
            1. Hate speech, discrimination, or offensive language
            2. Personal attacks or harassment
            3. Spam or promotional content
            4. False or misleading information
            5. Inappropriate or explicit content
            6. Off-topic or irrelevant content
            
            Review comment: "%s"
            
            Respond in this exact JSON format (no extra text):
            {
              "isViolated": true/false,
              "reason": "specific violation reason or empty string",
              "confidenceScore": 0.0-1.0
            }
            
            - If no violation: {"isViolated": false, "reason": "", "confidenceScore": 0.95}
            - If violation found: {"isViolated": true, "reason": "contains hate speech", "confidenceScore": 0.90}
            """, content);

        try {
            String response = chatClient.prompt(prompt).call().content();

            System.out.println("Text Moderation Response: " + response);
            // Parse JSON response
            response = response.trim()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // Simple JSON parsing
            boolean isViolated = response.contains("\"isViolated\": true");
            String reason = extractJsonValue(response, "reason");
            double confidenceScore = Double.parseDouble(extractJsonValue(response, "confidenceScore"));

            return new ContentModerationResult(isViolated, reason, confidenceScore);

        } catch (Exception e) {
            // In case of error, return not violated with low confidence
            return new ContentModerationResult(false, "AI moderation failed", 0.0);
        }
    }

    @Override
    public ContentModerationResult moderateImage(String imageUrl) {
        // If imageUrl is empty, return not violated
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return new ContentModerationResult(false, "", 1.0);
        }

        // Build prompt for AI image moderation
        /*
         * Image Censorship rules:
         * Inappropriate or explicit content            | Nội dung phản cảm, đồi trụy
         * Offensive or hateful imagery                 | Hình ảnh xúc phạm hoặc thù địch
         * Unrelated or spam content                    | Nội dung không liên quan hoặc spam
         * Misleading or fake product images            | Hình ảnh sản phẩm gây hiểu lầm hoặc giả mạo
         * Personal information or contact details      | Thông tin cá nhân hoặc chi tiết liên hệ
         */
        String prompt = String.format("""
            Analyze this product review image and determine if it contains any violations:
            
            1. Inappropriate or explicit content
            2. Offensive or hateful imagery
            3. Unrelated or spam content
            4. Misleading or fake product images
            5. Personal information or contact details
            
            Image URL: %s
            
            Respond in this exact JSON format (no extra text):
            {
              "isViolated": true/false,
              "reason": "specific violation reason or empty string",
              "confidenceScore": 0.0-1.0
            }
            
            Note: If you cannot access the image, return {"isViolated": false, "reason": "cannot analyze image", "confidenceScore": 0.0}
            """, imageUrl);

        try {
            String response = chatClient.prompt(prompt).call().content();

            System.out.println("Image Moderation Response: " + response);

            response = response.trim()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            boolean isViolated = response.contains("\"isViolated\": true");
            String reason = extractJsonValue(response, "reason");
            double confidenceScore = Double.parseDouble(extractJsonValue(response, "confidenceScore"));

            return new ContentModerationResult(isViolated, reason, confidenceScore);

        } catch (Exception e) {
            return new ContentModerationResult(false, "AI image moderation failed", 0.0);
        }
    }

    // Simple JSON value extractor
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\": \"";
        int start = json.indexOf(pattern);
        if (start == -1) {
            // Try without quotes for numbers/booleans
            pattern = "\"" + key + "\": ";
            start = json.indexOf(pattern);
            if (start == -1) return "";
            start += pattern.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
        start += pattern.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
