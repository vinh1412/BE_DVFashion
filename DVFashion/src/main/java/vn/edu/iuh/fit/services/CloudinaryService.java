/*
 * @ {#} CloudinaryService.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.services;

import org.springframework.web.multipart.MultipartFile;

/*
 * @description: Service interface for handling Cloudinary image uploads
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */
public interface CloudinaryService {
    /**
     * Uploads an image file to Cloudinary and returns the response.
     *
     * @param file the image file to upload
     * @return a CloudinaryResponse containing the upload details
     */
    String uploadImage(MultipartFile file);
}
