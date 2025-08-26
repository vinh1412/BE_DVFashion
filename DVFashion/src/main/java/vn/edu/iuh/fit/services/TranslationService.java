/*
 * @ {#} TranslationService.java   1.0     25/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

/*
 * @description: Service interface for translating text into different languages
 * @author: Tran Hien Vinh
 * @date:   25/08/2025
 * @version:    1.0
 */
public interface TranslationService {
    /**
     * Translates the given text into the specified target language.
     *
     * @param text the text to be translated
     * @param targetLang the target language code (e.g., "en" for English, "es" for Spanish)
     * @return the translated text
     */
    String translate(String text, String targetLang);
}
