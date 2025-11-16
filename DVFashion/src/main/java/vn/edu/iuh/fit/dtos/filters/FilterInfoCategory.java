/*
 * @ {#} FilterInfoCategory.java   1.0     15/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.filters;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/*
 * @description: Filter criteria for querying categories
 * @author: Tran Hien Vinh
 * @date:   15/11/2025
 * @version:    1.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterInfoCategory {
    private String search;

    private Boolean active;

    private Boolean hasProducts;

    private Long categoryId;
}
