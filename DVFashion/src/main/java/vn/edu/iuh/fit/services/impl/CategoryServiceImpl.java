/*
 * @ {#} CategoryServiceImpl.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.constants.SortFields;
import vn.edu.iuh.fit.dtos.filters.FilterInfoCategory;
import vn.edu.iuh.fit.dtos.request.CategoryRequest;
import vn.edu.iuh.fit.dtos.response.CategoryResponse;
import vn.edu.iuh.fit.dtos.response.CategoryStatisticsResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.entities.Category;
import vn.edu.iuh.fit.entities.CategoryTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.enums.ProductStatus;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.mappers.CategoryMapper;
import vn.edu.iuh.fit.repositories.CategoryRepository;
import vn.edu.iuh.fit.repositories.CategoryTranslationRepository;
import vn.edu.iuh.fit.services.CategoryService;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.TranslationService;
import vn.edu.iuh.fit.specifications.CategorySpecification;
import vn.edu.iuh.fit.utils.ImageUtils;
import vn.edu.iuh.fit.utils.SortUtils;
import vn.edu.iuh.fit.utils.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @description: Service implementation for managing categories
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    private  final CloudinaryService cloudinaryService;

    private final CategoryTranslationRepository translationRepository;

    private final TranslationService translationService;

    private final CategoryMapper categoryMapper;

    private final CategorySpecification categorySpecification;

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest, MultipartFile imageFile, Language inputLang) {
        // Check if the category with the same name already exists
        if(translationRepository.existsByNameIgnoreCaseAndLanguage(categoryRequest.name().toLowerCase(), inputLang)) {
            throw new AlreadyExistsException("Category with name '" + categoryRequest.name() + "' already exists.");
        }

        // Create the image URL using the Cloudinary service
        String imageUrl = ImageUtils.getImageUrl(imageFile, cloudinaryService);

        // Save the category entity
        Category category = Category.builder()
                .image(imageUrl)
                .active(categoryRequest.active() == null || categoryRequest.active()) // default true
                .build();
        categoryRepository.save(category);

        // Description for input language
        String descInput;
        if (categoryRequest.description() != null) {
            descInput = categoryRequest.description();
        } else {
            descInput = (inputLang == Language.VI) ? "Không có mô tả" : "No description";
        }

        // Save the input language translation
        CategoryTranslation inputTranslation  = CategoryTranslation.builder()
                .category(category)
                .language(inputLang)
                .name(categoryRequest.name())
                .description(descInput)
                .build();
        translationRepository.save(inputTranslation);

        // Determine the target language for translation
        Language targetLang = (inputLang == Language.VI) ? Language.EN : Language.VI;

        // Description for target language
        String descTarget;
        if (categoryRequest.description() != null) {
            descTarget = translationService.translate(categoryRequest.description(), targetLang.name());
        } else {
            descTarget = (targetLang == Language.VI) ? "Không có mô tả" : "No description";
        }

        // Translate and save the target language translation
        CategoryTranslation translatedTranslation  = CategoryTranslation.builder()
                .category(category)
                .language(targetLang)
                .name(translationService.translate(categoryRequest.name(), targetLang.name()))
                .description(descTarget)
                .build();
        translationRepository.save(translatedTranslation );

        // Map the saved entity back to the response DTO
        return categoryMapper.toResponse(category, inputLang);
    }

    @Override
    public CategoryResponse getCategoryById(Long id, Language language) {
        // Retrieve the category by ID
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        // Map the entity to the response DTO
        return categoryMapper.toResponse(category, language);
    }

    @Override
    public CategoryResponse updateCategory(CategoryRequest categoryRequest, Long id, MultipartFile imageFile, Language language) {
        // Find the existing category by ID
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        // Check if the category with the same name already exists, excluding the current category
        if (categoryRequest.name() != null) {
            // Check if name exists in current language
            boolean exists = translationRepository.existsByNameIgnoreCaseAndLanguage(categoryRequest.name().toLowerCase(), language);
            boolean isSame = category.getTranslations().stream()
                    // Check if the existing translation matches the new name and language
                    .anyMatch(t -> t.getLanguage() == language  && t.getName().equalsIgnoreCase(categoryRequest.name()));

            // If a different category with the same name exists, throw an exception
            if (exists && !isSame) {
                throw new AlreadyExistsException("Category with name '" + categoryRequest.name() + "' already exists.");
            }
        }

        // If an image file is provided, update the image URL
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = ImageUtils.getImageUrl(imageFile, cloudinaryService);
            category.setImage(imageUrl);
        }

        // Update the active status if provided
        if (categoryRequest.active() != null) {
            category.setActive(categoryRequest.active());
        }

        // Update translations for input language
        updateOrCreateTranslation(category, language, categoryRequest.name(), categoryRequest.description());

        // Determine the other language
        Language otherLang = (language == Language.EN) ? Language.VI : Language.EN;

        String otherName = categoryRequest.name() != null && !categoryRequest.name().isBlank()
                ? translationService.translate(categoryRequest.name(), otherLang.name())
                : null; // null means no update, the updateOrCreateTranslation function will remain the same

        String otherDesc = categoryRequest.description() != null && !categoryRequest.description().isBlank()
                ? translationService.translate(categoryRequest.description(), otherLang.name())
                : null; // null means no update, the updateOrCreateTranslation function will remain the same

        // Update translations for the other language
        updateOrCreateTranslation(category, otherLang,
                otherName,
                otherDesc);

        // Save the updated category
        categoryRepository.save(category);

        // Map the updated entity back to the response DTO
        return categoryMapper.toResponse(category, language);
    }


    // Helper method to update or create a translation
    private void updateOrCreateTranslation(Category category, Language lang, String name, String description) {
        // Find existing translation or create a new one
        CategoryTranslation translation = category.getTranslations().stream()
                .filter(t -> t.getLanguage() == lang)
                .findFirst()
                .orElseGet(() -> {
                    CategoryTranslation t = CategoryTranslation.builder()
                            .language(lang)
                            .category(category)
                            .build();
                    category.getTranslations().add(t);
                    return t;
                });

        // Update fields if new values are provided
        if (name != null && !name.isBlank()) {
            translation.setName(name);
        }
        if (description != null && !description.isBlank()) {
            translation.setDescription(description);
        }
    }

    @Override
    public void deactivateCategory(Long id) {
        // Retrieve the existing category by ID
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        // Set the category as inactive
        category.setActive(false);

        // Save the updated category
        categoryRepository.save(category);
    }

    @Override
    public PageResponse<CategoryResponse> getCategoriesPaging(Pageable pageable, Language language) {
        // Retrieve all categories with pagination
        Page<Category> categories = categoryRepository.findAll(pageable);

        // Map each Category entity to CategoryResponse DTO
        Page<CategoryResponse> dtoPage = categories.map(category -> {
            // Find the translation for the requested language
            CategoryTranslation translation = category.getTranslations().stream()
                    .filter(t -> t.getLanguage() == language)
                    .findFirst()
                    // If not found, fallback to Vietnamese, if still not found, take any available translation
                    .orElseGet(() -> category.getTranslations().stream()
                            .filter(t -> t.getLanguage() == Language.VI)
                            .findFirst()
                            .orElse(category.getTranslations().stream().findFirst()
                                    .orElseThrow(() -> new NotFoundException(
                                            "No translation found for category " + category.getId()
                                    ))
                            )
                    );

            // Map Category + Translation to DTO
            return new CategoryResponse(
                    category.getId(),
                    TextUtils.removeTrailingDot(translation.getName()),
                    translation.getDescription(),
                    category.getImage(),
                    category.isActive()
            );
        });

        // Convert the Page<CategoryResponse> to PageResponse<CategoryResponse>
        return PageResponse.from(dtoPage);
    }

    @Override
    public List<CategoryResponse> getAllCategories(Language language) {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(category -> categoryMapper.toResponse(category, language))
                .toList();
    }

    @Override
    public CategoryStatisticsResponse getCategoryStatistics() {
        // Get total number of categories
        long totalCategories = categoryRepository.countTotalCategories();

        // Get total number of active categories
        long totalActiveCategories = categoryRepository.countCategoriesByActiveStatus(true);

        // Get total number of inactive categories
        long totalInactiveCategories = categoryRepository.countCategoriesByActiveStatus(false);

        // Get total number of categories with any products
        long totalCategoriesWithProducts = categoryRepository.countCategoriesWithAnyProducts();

        // Get total number of categories with active products
        long totalCategoriesWithActiveProducts = categoryRepository.countCategoriesWithProducts(ProductStatus.ACTIVE);

        // Initialize map with all active statuses set to 0
        Map<Boolean, Long> categoriesByActiveStatus = new HashMap<>();
        categoriesByActiveStatus.put(true, 0L);
        categoriesByActiveStatus.put(false, 0L);

        // Get actual counts and override the default 0 values
        List<Object[]> statusCounts = categoryRepository.countCategoriesByAllActiveStatuses();
        statusCounts.forEach(row -> {
            Boolean active = (Boolean) row[0];
            Long count = (Long) row[1];
            categoriesByActiveStatus.put(active, count);
        });

        return CategoryStatisticsResponse.builder()
                .totalCategories(totalCategories)
                .totalActiveCategories(totalActiveCategories)
                .totalInactiveCategories(totalInactiveCategories)
                .totalCategoriesWithProducts(totalCategoriesWithProducts)
                .totalCategoriesWithActiveProducts(totalCategoriesWithActiveProducts)
                .categoriesByActiveStatus(categoriesByActiveStatus)
                .build();
    }

    @Override
    public PageResponse<CategoryResponse> getAllCategories(int page, int size, String[] sort, String search, Boolean active, Boolean hasProducts, Language language) {
        Sort validSort = SortUtils.buildSort(
                sort,
                SortFields.CATEGORY_SORT_FIELDS,
                SortFields.DEFAULT_CATEGORY_SORT
        );

        Pageable pageable = PageRequest.of(page, size, validSort);

        Specification<Category> spec = categorySpecification.build(
                search, active, hasProducts
        );

        Page<Category> pageData = categoryRepository.findAll(spec, pageable);

        Page<CategoryResponse> responses = pageData.map(c -> categoryMapper.toResponse(c, language));

        FilterInfoCategory filter = FilterInfoCategory.builder()
                .search(search)
                .active(active)
                .hasProducts(hasProducts)
                .build();

        return PageResponse.from(responses, filter);
    }
}
