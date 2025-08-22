/*
 * @ {#} ImageUtil.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.services.CloudinaryService;

/*
 * @description: Utility class for handling image uploads and providing default image URLs.
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */
public class ImageUtils {
    private static final String IMAGE_DEFAULT_URL = "https://res.cloudinary.com/diilgkg1a/image/upload/v1755791598/no-image.png";

    /**
     * Get the image URL from MultipartFile, if no image then return default URL.
     *
     * @param imageFile        MultipartFile containing the image
     * @param cloudinaryService Cloudinary service for uploading photos
     * @return URL of uploaded image or default URL if no image
     */
    public static String getImageUrl(MultipartFile imageFile, CloudinaryService cloudinaryService) {
        // Check if the image file is null or empty
        if (imageFile == null || imageFile.isEmpty()) {
            return IMAGE_DEFAULT_URL;
        }

        // Validate file size (limit 3MB)
        long maxSize = 3 * 1024 * 1024; // 3MB
        if (imageFile.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed limit of 5MB");
        }

        // Validate the content type of the image file
        String contentType = imageFile.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/octet-stream"))) {
            throw new IllegalArgumentException("File is not an image");
        }

        // Upload the image to Cloudinary and return the URL
        return cloudinaryService.uploadImage(imageFile);
    }
}
