/*
 * @ {#} CloudinaryServiceImpl.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.services.CloudinaryService;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/*
 * @description: Service implementation for handling Cloudinary image uploads
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) {
        String contentType = file.getContentType();

        // Check if the file is null or empty, or if the content type is not an image
        if (!(contentType != null ||
                contentType.startsWith("image/") &&
                        Objects.equals(contentType, "application/octet-stream"))) {
            throw new IllegalArgumentException("File is not an image");
        }

        try {
            // Upload the image to Cloudinary and return the secure URL
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Upload image to Cloudinary failed", e);
        }
    }
}
