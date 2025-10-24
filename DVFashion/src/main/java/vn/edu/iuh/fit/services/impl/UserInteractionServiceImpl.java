/*
 * @ {#} UserInteractionServiceImpl.java   1.0     19/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.entities.Product;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.entities.UserProductInteraction;
import vn.edu.iuh.fit.enums.InteractionType;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.repositories.ProductRepository;
import vn.edu.iuh.fit.repositories.UserProductInteractionRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.UserInteractionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

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
}
