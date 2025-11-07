/*
 * @ {#} SortUtils.java   1.0     07/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * @description: Utility class for building Sort objects from request parameters
 * @author: Tran Hien Vinh
 * @date:   07/11/2025
 * @version:    1.0
 */
@UtilityClass
public class SortUtils {
    // Build Sort object from sort parameters
    public Sort buildSort(String[] sort, Set<String> allowedFields, String defaultSortField) {
        List<Sort.Order> orders = new ArrayList<>();

        if (sort != null && sort.length > 0) {
            // Handle case where parameters might be split by comma
            if (sort.length == 2 && !sort[1].contains(",") &&
                    ("asc".equalsIgnoreCase(sort[1]) || "desc".equalsIgnoreCase(sort[1]))) {
                // Case: ?sort=name,desc gets parsed as ["name", "desc"]
                String sortParam = sort[0] + "," + sort[1];
                processSort(sortParam, orders, allowedFields);
            } else {
                // Normal case: each element is complete "property,direction"
                for (String param : sort) {
                    processSort(param, orders, allowedFields);
                }
            }
        }

        // If no valid sort provided, default to specified field DESC
        return orders.isEmpty()
                ? Sort.by(Sort.Direction.DESC, defaultSortField)
                : Sort.by(orders);
    }

    // Helper method to process individual sort parameter
    private void processSort(String sortParam, List<Sort.Order> orders, Set<String> allowedFields) {
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();

        // Check if this is a valid property field
        if (!allowedFields.contains(property)) {
            throw new IllegalArgumentException(
                    String.format("Sort field '%s' is not supported. Allowed fields are: %s", property, allowedFields)
            );
        }

        // Get direction from second part, default to ASC
        Sort.Direction direction = Sort.Direction.ASC;

        if (parts.length > 1) {
            String directionStr = parts[1].trim().toLowerCase();
            if ("desc".equals(directionStr)) {
                direction = Sort.Direction.DESC;
            }
        }

        orders.add(new Sort.Order(direction, property));
    }
}
