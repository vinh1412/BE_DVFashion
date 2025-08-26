/*
 * @ {#} BrandServiceImpl.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.BrandRequest;
import vn.edu.iuh.fit.dtos.response.BrandResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.entities.Brand;
import vn.edu.iuh.fit.entities.BrandTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.BrandRepository;
import vn.edu.iuh.fit.repositories.BrandTranslationRepository;
import vn.edu.iuh.fit.services.BrandService;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.TranslationService;
import vn.edu.iuh.fit.utils.ImageUtils;
import vn.edu.iuh.fit.utils.TextUtils;

/*
 * @description: Service implementation for managing brands
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;

    private final CloudinaryService cloudinaryService;

    private final TranslationService translationService;

    private final BrandTranslationRepository brandTranslationRepository;

    @Override
    public BrandResponse createBrand(BrandRequest request, MultipartFile logoFile, Language inputLang) {
        // Check if brand with the same name already exists in the specified language
        if (brandTranslationRepository.existsByNameIgnoreCaseAndLanguage(request.name().toLowerCase(), inputLang)) {
            throw new AlreadyExistsException("Brand with name " + request.name() + " already exists");
        }

        // Create the logo using the Cloudinary service
        String logo = ImageUtils.getImageUrl(logoFile, cloudinaryService);

        // Create and save the brand entity
        Brand brand = Brand.builder()
                .logo(logo)
                .active(request.active() != null ? request.active() : true)
                .build();

        brandRepository.save(brand);

        // Create and save the brand translation for the input language
        String descInput = request.description() != null
                ? request.description()
                : (inputLang == Language.VI ? "Không có mô tả" : "No description");

        BrandTranslation brandTranslation = BrandTranslation.builder()
                .name(request.name())
                .description(descInput)
                .language(inputLang)
                .brand(brand)
                .build();
        brandTranslationRepository.save(brandTranslation);

        // Create and save the brand translation for the target language
        Language targetLang = (inputLang == Language.VI) ? Language.EN : Language.VI;

        String descTarget = request.description() != null
                ? translationService.translate(request.description(), targetLang.name())
                : (targetLang == Language.VI ? "Không có mô tả" : "No description");

        BrandTranslation brandTranslationTarget = BrandTranslation.builder()
                .name(translationService.translate(request.name(), targetLang.name()))
                .description(descTarget)
                .language(targetLang)
                .brand(brand)
                .build();
        brandTranslationRepository.save(brandTranslationTarget);

        // Convert to response DTO and return
        return toResponse(brand, inputLang);
    }

    @Override
    public BrandResponse getBrandById(Long id, Language language) {
        // Find the brand by ID
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand with id " + id + " not found"));

        // Convert to response DTO and return
        return toResponse(brand, language);
    }

    @Override
    public BrandResponse updateBrand(BrandRequest brandRequest, Long id, MultipartFile logoFile, Language language) {
        // Find the brand by ID
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand with id " + id + " not found"));

        // Check if the brand with the same name already exists, excluding the current brand
        if (brandRequest.name() != null) {
            // Check if name exists in current language
            boolean exists = brandTranslationRepository.existsByNameIgnoreCaseAndLanguage(brandRequest.name().toLowerCase(), language);
            boolean isSame = brand.getTranslations().stream()
                    // Check if the existing translation matches the new name and language
                    .anyMatch(t -> t.getLanguage() == language && t.getName().equalsIgnoreCase(brandRequest.name()));

            // If a different brand with the same name exists, throw an exception
            if (exists && !isSame) {
                throw new AlreadyExistsException("Brand with name '" + brandRequest.name() + "' already exists.");
            }
        }

        // Update the logo if a new file is provided
        if (logoFile != null && !logoFile.isEmpty()) {
            String logo = ImageUtils.getImageUrl(logoFile, cloudinaryService);
            brand.setLogo(logo);
        }

        // Update the active status if provided
        if (brandRequest.active() != null) {
            brand.setActive(brandRequest.active());
        }

        // Update translations for input language
        updateOrCreateTranslation(brand, language, brandRequest.name(), brandRequest.description());

        // Determine the other language
        Language otherLang = (language == Language.EN) ? Language.VI : Language.EN;

        String otherName = brandRequest.name() != null
                ? translationService.translate(brandRequest.name(), otherLang.name())
                : null; // null means no update, the updateOrCreateTranslation function will remain the same

        String otherDesc = brandRequest.description() != null
                ? translationService.translate(brandRequest.description(), otherLang.name())
                : null; // null means no update, the updateOrCreateTranslation function will remain the same

        // Update translations for the other language
        updateOrCreateTranslation(brand, otherLang,
                otherName,
                otherDesc);

        // Save the updated brand entity
        brandRepository.save(brand);

        // Convert to response DTO and return
        return toResponse(brand, language);
    }

    @Override
    public void deactivateBrand(Long id) {
        // Find the brand by ID
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand with id " + id + " not found"));

        // Set the brand as inactive
        brand.setActive(false);

        // Save the updated brand entity
        brandRepository.save(brand);
    }

    @Override
    public PageResponse<BrandResponse> getBrandsPaging(Pageable pageable, Language language) {
        // Retrieve all brands with pagination
        Page<Brand> brands = brandRepository.findAll(pageable);

        // Map each Brand entity to BrandResponse DTO
        Page<BrandResponse> dtoPage = brands.map(brand -> {
            // Find the translation for the requested language
            BrandTranslation translation = brand.getTranslations().stream()
                    .filter(t -> t.getLanguage() == language)
                    .findFirst()
                    // If not found, fallback to Vietnamese, if still not found, take any available translation
                    .orElseGet(() -> brand.getTranslations().stream()
                            .filter(t -> t.getLanguage() == Language.VI)
                            .findFirst()
                            .orElse(brand.getTranslations().stream().findFirst()
                                    .orElseThrow(() -> new NotFoundException(
                                            "No translation found for brand " + brand.getId()
                                    ))
                            )
                    );

            // Map Brand + Translation to DTO
            return new BrandResponse(
                    brand.getId(),
                    TextUtils.removeTrailingDot(translation.getName()),
                    translation.getDescription(),
                    brand.getLogo(),
                    brand.isActive()
            );
        });

        // Convert the Page<BrandResponse> to PageResponse<BrandResponse>
        return PageResponse.from(dtoPage);
    }

    // Helper method to update or create a translation
    private void updateOrCreateTranslation(Brand brand, Language lang, String name, String description) {
        // Find existing translation or create a new one
        BrandTranslation translation = brand.getTranslations().stream()
                .filter(t -> t.getLanguage() == lang)
                .findFirst()
                .orElseGet(() -> {
                    BrandTranslation t = BrandTranslation.builder()
                            .language(lang)
                            .brand(brand)
                            .build();
                    brand.getTranslations().add(t);
                    return t;
                });

        // Update fields if new values are provided
        if (name != null) {
            translation.setName(name);
        }
        if (description != null) {
            translation.setDescription(description);
        }
    }

    private BrandResponse toResponse(Brand brand, Language lang) {
        BrandTranslation translation = brandTranslationRepository
                // Find translation by requested language
                .findByBrandIdAndLanguage(brand.getId(), lang)
                // If not found, fallback to Vietnamese
                .orElseGet(() -> brandTranslationRepository
                        .findByBrandIdAndLanguage(brand.getId(), Language.VI)
                        .orElseThrow(() -> new NotFoundException("No translation found"))
                );

        // Map Brand + Translation to DTO
        return new BrandResponse(
                brand.getId(),
                TextUtils.removeTrailingDot(translation.getName()),
                translation.getDescription(),
                brand.getLogo(),
                brand.isActive()
        );
    }
}
