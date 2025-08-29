/*
 * @ {#} SizeMapper.java   1.0     30/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.SizeResponse;
import vn.edu.iuh.fit.entities.Size;

/*
 * @description: Mapper for converting Size entity to SizeResponse DTO
 * @author: Tran Hien Vinh
 * @date:   30/08/2025
 * @version:    1.0
 */
@Component
public class SizeMapper {
    public SizeResponse toResponse(Size size) {
        if (size == null) {
            return null;
        }

        return new SizeResponse(
                size.getId(),
                size.getSizeName(),
                size.getStockQuantity(),
                size.getProductVariant() != null ? size.getProductVariant().getId() : null
        );
    }
}
