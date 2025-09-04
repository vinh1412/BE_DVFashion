/*
 * @ {#} CategoryMapper.java   1.0     22/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.CategoryResponse;
import vn.edu.iuh.fit.entities.Category;
import vn.edu.iuh.fit.entities.CategoryTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.CategoryTranslationRepository;
import vn.edu.iuh.fit.utils.TextUtils;

/*
 * @description: Mapper interface for converting between Category entity and Category DTOs (request and response)
 * @author: Tran Hien Vinh
 * @date:   22/08/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class CategoryMapper {
    private final CategoryTranslationRepository translationRepository;

    public CategoryResponse toResponse(Category category, Language lang) {
        CategoryTranslation translation = translationRepository
                .findByCategoryIdAndLanguage(category.getId(), lang)
                .orElseGet(() -> translationRepository
                        .findByCategoryIdAndLanguage(category.getId(), Language.VI)
                        .orElseThrow(() -> new NotFoundException("No translation found"))
                );

        return new CategoryResponse(
                category.getId(),
                TextUtils.removeTrailingDot(translation.getName()),
                translation.getDescription(),
                category.getImage(),
                category.isActive()
        );
    }
}