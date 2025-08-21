/*
 * @ {#} CategoryRepository.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Category;

import java.util.Optional;

/*
 * @description: Repository interface for managing Category entities, extending JpaRepository for CRUD operations
 * @author: Tran Hien Vinh
 * @date:   21/08/2025
 * @version:    1.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * Finds a category by its ID.
     *
     * @param id the ID of the category
     * @return an Optional containing the found category, or empty if not found
     */
    Optional<Category> findById(Long id);

    /**
     * Checks if a category with the given name exists, ignoring case.
     *
     * @param name the name of the category to check
     * @return true if a category with the given name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
}
