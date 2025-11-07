/*
 * @ {#} PageResponse.java   1.0     22/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import org.springframework.data.domain.Page;

import java.util.List;

/*
 * @description: Response DTO for paginated
 * @author: Tran Hien Vinh
 * @date:   22/08/2025
 * @version:    1.0
 */
public record PageResponse<T>(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<String> sorts,
        List<T> values,
        Object filters,
        boolean last
) {

    /**
     * Factory method to create a PageResponse from a Spring Data Page object
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        List<String> sortInfos = page.getSort().isSorted()
                ? page.getSort().stream()
                .map(order -> order.getProperty() + ": " + order.getDirection())
                .toList()
                : null;

        return new PageResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                sortInfos,
                page.getContent(),
                null,
                page.isLast()
        );
    }

    public static <T> PageResponse<T> from(Page<T> page, Object filterInfo) {
        List<String> sortInfos = page.getSort().isSorted()
                ? page.getSort().stream()
                .map(order -> order.getProperty() + ": " + order.getDirection())
                .toList()
                : null;

        return new PageResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                sortInfos,
                page.getContent(),
                filterInfo,
                page.isLast()
        );
    }
}
