/*
 * @ {#} BrandMapper.java   1.0     04/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.BrandResponse;
import vn.edu.iuh.fit.entities.Brand;
import vn.edu.iuh.fit.entities.BrandTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.BrandTranslationRepository;
import vn.edu.iuh.fit.utils.TextUtils;

/*
 * @description: Mapper class for converting between Brand entity and Brand DTOs (request and response)
 * @author: Tran Hien Vinh
 * @date:   04/09/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class BrandMapper {
    private final BrandTranslationRepository brandTranslationRepository;

    public BrandResponse toResponse(Brand brand, Language lang) {
        BrandTranslation translation = brandTranslationRepository
                .findByBrandIdAndLanguage(brand.getId(), lang)
                .orElseGet(() -> brandTranslationRepository
                        .findByBrandIdAndLanguage(brand.getId(), Language.VI)
                        .orElseThrow(() -> new NotFoundException("No translation found"))
                );

        return new BrandResponse(
                brand.getId(),
                TextUtils.removeTrailingDot(translation.getName()),
                translation.getDescription(),
                brand.getLogo(),
                brand.isActive()
        );
    }
}
