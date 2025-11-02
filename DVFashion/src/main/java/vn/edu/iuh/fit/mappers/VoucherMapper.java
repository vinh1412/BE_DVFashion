/*
 * @ {#} VoucherMapper.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.VoucherOrderResponse;
import vn.edu.iuh.fit.dtos.response.VoucherProductResponse;
import vn.edu.iuh.fit.dtos.response.VoucherResponse;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.Voucher;
import vn.edu.iuh.fit.entities.VoucherTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.NotFoundException;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: Mapper class for converting Voucher entities to VoucherResponse DTOs
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
@Component
public class VoucherMapper {
    public VoucherResponse mapToResponse(Voucher voucher, Language language) {
        // Get translation for requested language, fallback to VI if not found
        VoucherTranslation translation = voucher.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .findFirst()
                .orElseGet(() -> voucher.getTranslations().stream()
                        .filter(t -> t.getLanguage() == Language.VI)
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("No translation found for voucher"))
                );

        List<VoucherProductResponse> productResponses = voucher.getVoucherProducts().stream()
                .map(vp -> new VoucherProductResponse(
                        vp.getId(),
                        vp.getProduct().getId(),
                        getProductName(vp.getProduct(), language),
                        vp.getActive()
                ))
                .toList();

        return new VoucherResponse(
                voucher.getId(),
                voucher.getType(),
                translation.getName(),
                voucher.getCode(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getAllowPreSave(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getHasMaxDiscount(),
                voucher.getMaxDiscountAmount(),
                voucher.getMinOrderAmount(),
                voucher.getMaxTotalUsage(),
                voucher.getMaxUsagePerUser(),
                voucher.getCurrentUsage(),
                voucher.getActive(),
                productResponses
        );
    }

    private String getProductName(Product product, Language language) {
        return product.getTranslations().stream()
                .filter(t -> t.getLanguage() == language)
                .map(t -> t.getName())
                .findFirst()
                .orElse("Unknown Product");
    }

    public VoucherOrderResponse mapToVoucherOrderResponse(Voucher voucher, BigDecimal discountAmount) {
        return new VoucherOrderResponse(
                voucher.getId(),
                voucher.getCode(),
                voucher.getDiscountType(),
                discountAmount
        );
    }
}
