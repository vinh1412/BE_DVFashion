/*
 * @ {#} FileUtils.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

/*
 * @description: Utility class for file operations
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
@UtilityClass
public class FileUtils {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    private static final String[] ALLOWED_VIDEO_TYPES = {"video/mp4", "video/avi", "video/mov", "video/wmv"};
    private static final String[] ALLOWED_FILE_TYPES = {"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};

    public static boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;

        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;

        for (String allowedType : ALLOWED_VIDEO_TYPES) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidFile(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) return false;

        return isValidImageFile(file) || isValidVideoFile(file) || isValidDocumentFile(file);
    }

    public static boolean isValidDocumentFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;

        for (String allowedType : ALLOWED_FILE_TYPES) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeInBytes / 1024.0);
        } else {
            return String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        }
    }
}
