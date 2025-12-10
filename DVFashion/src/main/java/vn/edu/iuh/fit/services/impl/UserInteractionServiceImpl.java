/*
 * @ {#} UserInteractionServiceImpl.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.dtos.response.ProductResponse;
import vn.edu.iuh.fit.dtos.response.UserProductInteractionResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.entities.UserProductInteraction;
import vn.edu.iuh.fit.enums.InteractionType;
import vn.edu.iuh.fit.enums.Language;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.repositories.UserProductInteractionRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.ProductService;
import vn.edu.iuh.fit.services.UserInteractionService;
import vn.edu.iuh.fit.services.UserService;
import vn.edu.iuh.fit.specifications.UserProductInteractionSpecification;
import vn.edu.iuh.fit.utils.LanguageUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * @description: Service implementation for tracking user interactions with products
 * @author: Tran Hien Vinh
 * @date:   19/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInteractionServiceImpl implements UserInteractionService {
    private final UserProductInteractionRepository interactionRepository;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void trackInteraction(Long userId, Long productId, InteractionType interactionType, BigDecimal rating) {
        // Find user and product
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + productId));

        // Check if interaction already exists
        Optional<UserProductInteraction> existingInteraction = interactionRepository
                .findByUser_IdAndProduct_IdAndInteractionType(userId, productId, interactionType);

        if (existingInteraction.isPresent()) {
            // Update existing interaction
            UserProductInteraction interaction = existingInteraction.get();
            interaction.setInteractionCount(interaction.getInteractionCount() + 1);

            if (rating != null) {
                interaction.setRating(rating);
            }

            interaction.setUpdatedAt(LocalDateTime.now());
            interactionRepository.save(interaction);

            log.info("Updated interaction: userId={}, productId={}, type={}, count={}",
                    userId, productId, interactionType, interaction.getInteractionCount());
        } else {
            // Create new interaction
            UserProductInteraction interaction = new UserProductInteraction();
            interaction.setUser(user);
            interaction.setProduct(product);
            interaction.setInteractionType(interactionType);
            interaction.setRating(rating);
            interaction.setInteractionCount(1);
            interaction.setCreatedAt(LocalDateTime.now());
            interaction.setUpdatedAt(LocalDateTime.now());

            interactionRepository.save(interaction);

            log.info("Created new interaction: userId={}, productId={}, type={}",
                    userId, productId, interactionType);
        }
    }

    @Override
    public PageResponse<UserProductInteractionResponse> findAllWithFilters(Long userId, Long productId, InteractionType interactionType, LocalDate fromDate, LocalDate toDate, int page, int size) {
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (fromDate != null) {
            fromDateTime = fromDate.atStartOfDay();
        }

        if (toDate != null) {
            toDateTime = toDate.atTime(23, 59, 59);
        }

        Specification<UserProductInteraction> spec = UserProductInteractionSpecification
                .filterBy(userId, productId, interactionType, fromDateTime, toDateTime);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserProductInteraction> interactions = interactionRepository.findAll(spec, pageable);

        Page<UserProductInteractionResponse> responsePage = interactions.map(this::mapToResponse);

        return PageResponse.from(responsePage);
    }


    // Maps a UserProductInteraction entity to a UserProductInteractionResponse DTO.
    private UserProductInteractionResponse mapToResponse(UserProductInteraction interaction) {
        return new UserProductInteractionResponse(
                interaction.getId(),
                interaction.getUser().getId(),
                interaction.getProduct().getId(),
                interaction.getInteractionType().toString(),
                interaction.getRating() != null ? interaction.getRating().toString() : null,
                interaction.getInteractionCount(),
                interaction.getCreatedAt().toString()
        );
    }
}
