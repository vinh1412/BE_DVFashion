/*
 * @ {#} TranslationServiceImpl.java   1.0     25/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.services.TranslationService;


/*
 * @description: Service implementation for translating text using a chat client
 * @author: Tran Hien Vinh
 * @date:   25/08/2025
 * @version:    1.0
 */
@Service
public class TranslationServiceImpl implements TranslationService {
    private final ChatClient chatClient;

    public TranslationServiceImpl(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    @Override
    public String translate(String text, String targetLang) {
        String prompt = String.format(
                "Translate the following text into %s. Respond with the translation only, in natural language, no explanation, no quotes, no extra words:\n\n%s",
                targetLang,
                text
        );

        String result = chatClient
                .prompt(prompt)
                .call()
                .content();

        // Can remove unnecessary ** or duplicate words
        if(result != null) {
            result = result.replaceAll("\\*\\*", "").trim();

            // If AI still returns the form: The translation of ... is ... → only take the part after "is"
            if(result.toLowerCase().contains(" is ")) {
                result = result.substring(result.indexOf(" is ") + 4).trim();
            }
        }

        return result;
    }
}
