/*
 * @ {#} SizeResponse.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: DTO for Size response
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
public record SizeResponse(
        Long id,

        String sizeName,

        int stockQuantity
) {}
