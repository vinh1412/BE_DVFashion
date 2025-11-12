/*
 * @ {#} CategoryRepository.java   1.0     21/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Category;
import vn.edu.iuh.fit.enums.ProductStatus;

import java.util.List;
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
//    boolean existsByNameIgnoreCase(String name);

    /**
     * Counts the total number of categories.
     *
     * @return the total number of categories
     */
    @Query("SELECT COUNT(c) FROM Category c")
    long countTotalCategories();

    /**
     * Counts the number of categories by their active status.
     *
     * @param active the active status to filter by
     * @return the number of categories with the specified active status
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.active = :active")
    long countCategoriesByActiveStatus(@Param("active") boolean active);

    /**
     * Counts the number of categories grouped by their active status.
     *
     * @return a list of Object arrays, each containing the active status and the corresponding count
     */
    @Query("SELECT c.active, COUNT(c) FROM Category c GROUP BY c.active")
    List<Object[]> countCategoriesByAllActiveStatuses();

    /**
     * Counts the number of categories that have products with the specified status.
     *
     * @param status the product status to filter by
     * @return the number of categories with products of the specified status
     */
    @Query("SELECT COUNT(DISTINCT p.category.id) FROM Product p WHERE p.status = :status")
    long countCategoriesWithProducts(@Param("status") ProductStatus status);

    /**
     * Counts the number of categories that have any products associated with them.
     *
     * @return the number of categories with at least one product
     */
    @Query("SELECT COUNT(DISTINCT p.category.id) FROM Product p")
    long countCategoriesWithAnyProducts();

}
