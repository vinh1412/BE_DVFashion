/*
 * @ {#} ContentModerationService.java   1.0     18/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.response.ContentModerationResult;

/*
 * @description: Service interface for content moderation functionalities
 * @author: Tran Hien Vinh
 * @date:   18/10/2025
 * @version:    1.0
 */
public interface ContentModerationService {
    /**
     * Moderates the given text content.
     *
     * @param content The text content to be moderated.
     * @return The result of the moderation process.
     */
    ContentModerationResult moderateText(String content);

    /**
     * Moderates the image at the given URL.
     *
     * @param imageUrl The URL of the image to be moderated.
     * @return The result of the moderation process.
     */
    ContentModerationResult moderateImage(String imageUrl);
}
