/*
 * @ {#} BrandRepository.java   1.0     27/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Brand;

import java.util.Optional;

/*
 * @description: Repository interface for managing Brand entities
 * @author: Tran Hien Vinh
 * @date:   27/08/2025
 * @version:    1.0
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    /**
     * Finds a brand by its ID.
     *
     * @param id the ID of the brand
     * @return an Optional containing the found brand, or empty if not found
     */
    Optional<Brand> findById(Long id);
}
