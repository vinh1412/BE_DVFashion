/*
 * @ {#} CategoryMapper.java   1.0     22/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import org.mapstruct.*;
import vn.edu.iuh.fit.dtos.request.CategoryRequest;
import vn.edu.iuh.fit.dtos.response.CategoryResponse;
import vn.edu.iuh.fit.entities.Category;

/*
 * @description: Mapper interface for converting between Category entity and Category DTOs (request and response)
 * @author: Tran Hien Vinh
 * @date:   22/08/2025
 * @version:    1.0
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {
    /**
     * Converts a Category entity to a CategoryResponse DTO.
     *
     * @param category the Category entity to convert
     * @return the converted CategoryResponse DTO
     */
    @Mapping(source = "image", target = "imageUrl") // image (Category) -> imageUrl (CategoryResponse)
    CategoryResponse toDto(Category category);

    /**
     * Converts a CategoryRequest DTO to a Category entity.
     *
     * @param request the CategoryRequest DTO to convert
     * @return the converted Category entity
     */
    @Mapping(source = "imageUrl", target = "image") // imageUrl (CategoryRequest) -> image (Category)
    Category toEntity(CategoryRequest request);

    /**
     * Updates an existing Category entity with values from a CategoryRequest DTO.
     * Only non-null properties in the DTO will be applied to the entity
     *
     * @param request the CategoryRequest DTO containing new values
     * @param category the existing Category entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CategoryRequest request, @MappingTarget Category category);
}
