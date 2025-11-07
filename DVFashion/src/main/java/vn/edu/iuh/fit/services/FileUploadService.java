/*
 * @ {#} FileUploadService.java   1.0     06/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.web.multipart.MultipartFile;

/*
 * @description: Service interface for file upload functionalities
 * @author: Tran Hien Vinh
 * @date:   06/11/2025
 * @version:    1.0
 */
public interface FileUploadService {
    /**
     * Upload a file to the specified folder.
     *
     * @param file   the file to be uploaded
     * @param folder the target folder for the upload
     * @return the public ID of the uploaded file
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Delete a file by its public ID.
     *
     * @param publicId the public ID of the file to be deleted
     */
    void deleteFile(String publicId);

    /**
     * Get the URL of a file by its public ID.
     *
     * @param publicId the public ID of the file
     * @return the URL of the file
     */
    String getFileUrl(String publicId);
}
