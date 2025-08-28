/*
 * @ {#} ProductService.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ProductRequest;
import vn.edu.iuh.fit.dtos.response.ProductResponse;
import vn.edu.iuh.fit.enums.Language;

import java.util.List;

/*
 * @description: Service interface for managing Products
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public interface ProductService {
    /**
     * Creates a new product with the given request data, input language, and variant images.
     *
     * @param request       The product request data.
     * @param inputLang     The language of the input data.
     * @param variantImages A list of images for the product variants.
     * @return The created product response.
     */
    ProductResponse createProduct(ProductRequest request, Language inputLang, List<MultipartFile> variantImages);
}
