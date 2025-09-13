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

import java.util.List;
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


    /**
     * Retrieves all inventory records along with associated product information.
     * This method uses JOIN FETCH to eagerly load related entities to avoid N+1 query issues.
     *
     * @return a list of all inventory records with product details
     */
    @Query("""
                SELECT i FROM Inventory i 
                JOIN FETCH i.size s 
                JOIN FETCH s.productVariant pv 
                JOIN FETCH pv.product p 
                JOIN FETCH p.translations
                ORDER BY p.id, pv.id, s.id
            """)
    List<Inventory> findAllWithProductInfo();


    /**
     * Counts the number of inventory items that are considered low in stock.
     * An item is considered low in stock if its available quantity
     * (quantityInStock - reservedQuantity) is less than or equal to its minimum stock level.
     *
     * @return the count of low stock inventory items
     */
    @Query("""
                SELECT COUNT(i) FROM Inventory i
                WHERE (i.quantityInStock - i.reservedQuantity) <= i.minStockLevel
            """)
    Long countLowStockItems();

    /**
     * Retrieves the total stock quantity across all inventory items.
     * This represents the sum of the 'quantityInStock' field from all inventories.
     *
     * @return the total stock quantity
     */
    @Query("""
                SELECT SUM(i.quantityInStock) FROM Inventory i
            """)
    Long getTotalStockQuantity();

    /**
     * Retrieves the total reserved quantity across all inventory items.
     * This represents the sum of the 'reservedQuantity' field from all inventories.
     *
     * @return the total reserved quantity
     */
    @Query("""
                SELECT SUM(i.reservedQuantity) FROM Inventory i
            """)
    Long getTotalReservedQuantity();

    /**
     * Counts the number of inventory items that are out of stock.
     * An item is considered out of stock if its available quantity
     * (quantityInStock - reservedQuantity) is equal to zero.
     *
     * @return the count of out of stock inventory items
     */
    @Query("""
                SELECT COUNT(i) FROM Inventory i
                WHERE (i.quantityInStock - i.reservedQuantity) = 0
            """)
    Long countOutOfStockItems();

    /**
     * Finds inventory items that are low in stock.
     * An item is considered low in stock if its available quantity
     * (quantityInStock - reservedQuantity) is less than or equal to its minimum stock level.
     * The results are ordered by available quantity in ascending order.
     *
     * @return a list of low stock inventory items
     */
    @Query("""
                SELECT i FROM Inventory i
                JOIN FETCH i.size s
                JOIN FETCH s.productVariant pv
                JOIN FETCH pv.product p
                JOIN FETCH p.translations
                WHERE (i.quantityInStock - i.reservedQuantity) <= i.minStockLevel
                ORDER BY (i.quantityInStock - i.reservedQuantity) ASC
            """)
    List<Inventory> findLowStockItems();

}
