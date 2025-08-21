/*
 * @ {#} CategoryRequest.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

/*
 * @description: Request DTO for creating or updating a category, including fields for name, description, image URL, and active status
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private String imageUrl;

    private boolean active= true;
}
