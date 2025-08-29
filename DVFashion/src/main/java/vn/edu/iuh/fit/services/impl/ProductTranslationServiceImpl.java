/*
 * @ {#} ProductTranslationServiceImpl.java   1.0     28/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.dtos.request.ProductRequest;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.ProductTranslation;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.AlreadyExistsException;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.repositories.ProductTranslationRepository;
import vn.edu.iuh.fit.services.ProductTranslationService;
import vn.edu.iuh.fit.services.TranslationService;

/*
 * @description: Service implementation for managing Product Translations
 * @author: Tran Hien Vinh
 * @date:   28/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class ProductTranslationServiceImpl implements ProductTranslationService {
    private final ProductTranslationRepository productTranslationRepository;

    private final ProductRepository productRepository;

    private final TranslationService translationService;

    @Transactional
    @Override
    public void createProductTranslations(Product product, ProductRequest request, Language inputLang) {
        // Check if Product exists
        Product existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + product.getId()));

        // Check if ProductTranslation with the same name and language already exists
        boolean exists = productTranslationRepository.existsByNameIgnoreCaseAndLanguage(request.name().toLowerCase(), inputLang);
        if (exists) {
            throw new AlreadyExistsException("Product already exists with name: " + request.name());
        }

        // Create and save ProductTranslation for input language
        String descInput;
        if (request.description() != null) {
            descInput = request.description();
        } else {
            descInput = (inputLang == Language.VI) ? "Không có mô tả" : "No description";
        }

        ProductTranslation productTranslation = ProductTranslation.builder()
                .product(existingProduct)
                .language(inputLang)
                .name(request.name())
                .description(descInput)
                .material(request.material())
                .build();

        productTranslationRepository.save(productTranslation);

        // Determine target language for translation
        Language targetLang = (inputLang == Language.VI) ? Language.EN : Language.VI;

        // Create and save ProductTranslation for target language
        String descTarget;
        if (request.description() != null) {
            descTarget = translationService.translate(request.description(), targetLang.getValue());
        } else {
            descTarget = (targetLang == Language.VI) ? "Không có mô tả" : "No description";
        }

        ProductTranslation translatedTranslation = ProductTranslation.builder()
                .product(existingProduct)
                .language(targetLang)
                .name(translationService.translate(request.name(), targetLang.getValue()))
                .description(descTarget)
                .material(translationService.translate(request.material(), targetLang.getValue()))
                .build();

        productTranslationRepository.save(translatedTranslation);
    }

    @Transactional
    @Override
    public void updateProductTranslations(Product product, ProductRequest request, Language inputLang) {
        // Check if Product exists
        Product existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + product.getId()));

        // Find existing ProductTranslation for input language
        ProductTranslation productTranslation = productTranslationRepository.findByProductIdAndLanguage(existingProduct.getId(), inputLang)
                .orElse(ProductTranslation.builder()
                        .product(existingProduct)
                        .language(inputLang)
                        .build());

        // Check for name uniqueness if name is being updated
        if (request.name() != null) {
            // Check if another ProductTranslation with the same name and language exists
            boolean exists = productTranslationRepository.existsByNameIgnoreCaseAndLanguage(request.name().toLowerCase(), inputLang);

            boolean isSameName = product.getTranslations().stream()
                    .anyMatch(t -> t.getLanguage() == inputLang && t.getName().equalsIgnoreCase(request.name()));

            // If exists and it's not the same translation, throw exception
            if (exists && !isSameName) {
                throw new AlreadyExistsException("Product already exists with name: " + request.name());
            }

            productTranslation.setName(request.name());
        }

        if (request.description() != null){
            productTranslation.setDescription(request.description());
        }

        if (request.material() != null){
            productTranslation.setMaterial(request.material());
        }

        productTranslationRepository.save(productTranslation);

        // Determine target language for translation
        Language targetLang = (inputLang == Language.VI) ? Language.EN : Language.VI;

        // Update or create ProductTranslation for target language
        ProductTranslation translatedTranslation = productTranslationRepository
                .findByProductIdAndLanguage(existingProduct.getId(), targetLang)
                .orElse(ProductTranslation.builder()
                        .product(existingProduct)
                        .language(targetLang)
                        .build());

        // Update fields if they are provided in the request
        if (translatedTranslation != null) {
            if (request.name() != null) {
                translatedTranslation.setName(translationService.translate(request.name(), targetLang.getValue()));
            }

            if (request.description() != null) {
                translatedTranslation.setDescription(translationService.translate(request.description(), targetLang.getValue()));
            }

            if (request.material() != null) {
                translatedTranslation.setMaterial(translationService.translate(request.material(), targetLang.getValue()));
            }

            productTranslationRepository.save(translatedTranslation);
        }
    }
}
