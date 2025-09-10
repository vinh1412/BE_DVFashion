/*
 * @ {#} InventoryRepository.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Inventory;

import java.util.Optional;

/*
 * @description: Repository interface for inventory management
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    /**
     * Finds an inventory record by size ID with a pessimistic write lock.
     * Lock is used to prevent concurrent modifications.
     *
     * @param sizeId the ID of the size
     * @return an Optional containing the found inventory record, or empty if not found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.size.id = :sizeId")
    Optional<Inventory> findBySizeIdWithLock(@Param("sizeId") Long sizeId);

    /**
     * Finds an inventory record by size ID.
     *
     * @param sizeId the ID of the size
     * @return an Optional containing the found inventory record, or empty if not found
     */
    Optional<Inventory> findBySizeId(Long sizeId);
}
