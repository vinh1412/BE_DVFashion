/*
 * @ {#} FileUploadServiceImpl.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.exceptions.BadRequestException;
import vn.edu.iuh.fit.services.FileUploadService;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            String publicId = folder + "/" + uniqueFilename;

            // Upload options
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto", // Auto-detect resource type (image, video, raw)
                    "folder", folder,
                    "use_filename", false,
                    "unique_filename", true,
                    "overwrite", false
            );

            // Perform upload
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            String uploadedUrl = (String) uploadResult.get("secure_url");
            log.info("File uploaded successfully: {}", uploadedUrl);

            return uploadedUrl;

        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary", e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String publicId) {
        try {
            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted successfully: {}", publicId);
        } catch (IOException e) {
            log.error("Error deleting file from Cloudinary: {}", publicId, e);
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String publicId) {
        return cloudinary.url().publicId(publicId).secure(true).generate();
    }
}
