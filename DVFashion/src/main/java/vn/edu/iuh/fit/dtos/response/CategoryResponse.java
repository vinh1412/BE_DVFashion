/*
 * @ {#} CategoryRequest.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description: Response DTO for category details, including ID, name, description, image URL, and active status
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */

public record CategoryResponse(
        Long id,

        String name,

        String description,

        String imageUrl,

        boolean active
) {}

