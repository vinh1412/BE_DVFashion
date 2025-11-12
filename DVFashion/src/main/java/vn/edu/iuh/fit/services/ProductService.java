/*
 * @ {#} ProductService.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ProductRequest;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.dtos.response.ProductResponse;
import vn.edu.iuh.fit.dtos.response.ProductStatisticsResponse;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    /**
     * Updates an existing product identified by productId with the given request data, input language, and variant images.
     *
     * @param productId     The ID of the product to update.
     * @param request       The updated product request data.
     * @param inputLang     The language of the input data.
     * @return The updated product response.
     */
    ProductResponse updateProduct(Long productId, ProductRequest request, Language inputLang);

    /**
     * Retrieves a product by its ID and returns its details in the specified language.
     *
     * @param productId The ID of the product to retrieve.
     * @param language  The language for the product details.
     * @return The product response containing the product details.
     */
    ProductResponse getProductById(Long productId, Language language);

    /**
     * Retrieves all products in the specified language.
     *
     * @param language The language for the product details.
     * @return List of product responses containing all products.
     */
    List<ProductResponse> getAllProducts(Language language);

    /**
     * Retrieves a paginated list of products in the specified language.
     *
     * @param pageable Pagination information.
     * @param language The language for the product details.
     * @return A paginated response containing product responses.
     */
    PageResponse<ProductResponse> getProductsPaging(Pageable pageable, Language language);

    /**
     * Retrieves products associated with a specific promotion ID in the specified language.
     *
     * @param promotionId The ID of the promotion.
     * @param language    The language for the product details.
     * @return List of product responses associated with the promotion.
     */
    List<ProductResponse> getProductsByPromotionId(Long promotionId, Language language);

    /**
     * Retrieves a paginated list of products associated with a specific promotion ID in the specified language.
     *
     * @param promotionId The ID of the promotion.
     * @param pageable    Pagination information.
     * @param language    The language for the product details.
     * @return A paginated response containing product responses associated with the promotion.
     */
    PageResponse<ProductResponse> getProductsByPromotionIdPaging(Long promotionId, Pageable pageable, Language language);

    /**
     * Retrieves products associated with a specific category ID in the specified language.
     *
     * @param categoryId The ID of the category.
     * @param language   The language for the product details.
     * @return List of product responses associated with the category.
     */
    List<ProductResponse> getProductsByCategoryId(Long categoryId, Language language);

    /**
     * Retrieves a paginated list of products associated with a specific category ID in the specified language.
     *
     * @param categoryId The ID of the category.
     * @param pageable   Pagination information.
     * @param language   The language for the product details.
     * @return A paginated response containing product responses associated with the category.
     */
    PageResponse<ProductResponse> getProductsByCategoryIdPaging(Long categoryId, Pageable pageable, Language language);

    /**
     * Retrieves a paginated list of products based on various filtering criteria in the specified language.
     *
     * @param page         The page number to retrieve.
     * @param size         The number of products per page.
     * @param sort         Sorting criteria.
     * @param search       Search keyword for product name.
     * @param categoryId   Filter by category ID.
     * @param promotionId  Filter by promotion ID.
     * @param status       Filter by product status.
     * @param onSale       Filter by on-sale status.
     * @param minPrice     Minimum price filter.
     * @param maxPrice     Maximum price filter.
     * @param startDate    Start date filter for creation date.
     * @param endDate      End date filter for creation date.
     * @param language     The language for the product details.
     * @return A paginated response containing filtered product responses.
     */
    PageResponse<ProductResponse> getAllProducts(int page, int size, String[] sort,
                                                 String search, Long categoryId, Long promotionId,
                                                 ProductStatus status, Boolean onSale,
                                                 BigDecimal minPrice, BigDecimal maxPrice,
                                                 LocalDate startDate, LocalDate endDate,
                                                 Language language);

    /**
     * Retrieves product statistics.
     *
     * @return ProductStatisticsResponse containing various statistics about products.
     */
    ProductStatisticsResponse getProductStatistics();
}
