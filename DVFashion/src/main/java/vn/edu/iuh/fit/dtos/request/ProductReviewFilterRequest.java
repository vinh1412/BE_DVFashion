/*
 * @ {#} ProductReviewFilterRequest.java   1.0     18/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

/*
 * @description: DTO class for filtering product reviews on the product detail page.
 * @author: Tran Hien Vinh
 * @date:   18/10/2025
 * @version:    1.0
 */
public record ProductReviewFilterRequest(
        Integer rating,

        Boolean hasImages,

        Boolean hasComment
) {}
