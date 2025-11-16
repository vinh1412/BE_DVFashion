/*
 * @ {#} SortFields.java   1.0     07/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.constants;

import lombok.experimental.UtilityClass;

import java.util.Set;

/*
 * @description: Utility class defining valid sort fields for products
 * @author: Tran Hien Vinh
 * @date:   07/11/2025
 * @version:    1.0
 */
@UtilityClass
public class SortFields {
    public static final Set<String> PRODUCT_SORT_FIELDS =
            Set.of("id", "price", "salePrice", "createdAt", "updatedAt", "name");

    public static final String DEFAULT_PRODUCT_SORT = "createdAt";

    public static final Set<String> CATEGORY_SORT_FIELDS =
            Set.of("id", "active");

    public static final String DEFAULT_CATEGORY_SORT = "id";
}
