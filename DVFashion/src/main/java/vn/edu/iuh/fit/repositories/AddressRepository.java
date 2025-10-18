/*
 * @ {#} AddressRepository.java   1.0     03/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Address;

import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for managing Address entities in the database.
 * @author: Tran Hien Vinh
 * @date:   03/10/2025
 * @version:    1.0
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    /**
     * Clears the default address for a specific user by setting isDefault to false
     * for all addresses of that user.
     *
     * @param userId the ID of the user whose default address should be cleared
     */
    @Modifying
    @Query("update Address a set a.isDefault = false where a.user.id = :userId and a.isDefault = true")
    void clearDefaultForUser(Long userId);

    /**
     * Checks if a duplicate address exists for a user based on the provided shipping information.
     *
     * @param userId   the ID of the user
     * @param phone    the phone number in the shipping information
     * @param country  the country in the shipping information
     * @param city     the city in the shipping information
     * @param district the district in the shipping information
     * @param ward     the ward in the shipping information
     * @param street   the street in the shipping information
     * @return true if a duplicate address exists, false otherwise
     */
    @Query("""
        select (count(a) > 0) from Address a
        where a.user.id = :userId
          and a.id != :excludeId
          and a.isDeleted = false
          and a.shippingInfo.phone = :phone
          and lower(a.shippingInfo.country) = lower(:country)
          and lower(a.shippingInfo.city) = lower(:city)
          and lower(a.shippingInfo.district) = lower(:district)
          and lower(a.shippingInfo.ward) = lower(:ward)
          and lower(a.shippingInfo.street) = lower(:street)
        """)
    boolean existsDuplicate(Long userId, Long excludeId, String phone, String country, String city, String district, String ward, String street);

    /**
     * Finds an address by its ID and the associated user's ID.
     *
     * @param id     the ID of the address
     * @param userId the ID of the user
     * @return an Optional containing the found Address, or empty if not found
     */
    Optional<Address> findByIdAndUserId(Long id, Long userId);

    /**
     * Retrieves all non-deleted addresses for a specific user, sorted by the provided Sort object.
     *
     * @param userId the ID of the user
     * @param sort   the Sort object defining the sorting order
     * @return a list of non-deleted addresses for the user
     */
    @Query("select a from Address a where a.user.id = :userId and a.isDeleted = false")
    List<Address> findAllByUserIdAndIsDeletedFalse(Long userId, Sort sort);

    /**
     * Finds a soft-deleted duplicate address for a user based on the provided shipping information.
     *
     * @param userId   the ID of the user
     * @param phone    the phone number in the shipping information
     * @param country  the country in the shipping information
     * @param city     the city in the shipping information
     * @param district the district in the shipping information
     * @param ward     the ward in the shipping information
     * @param street   the street in the shipping information
     * @return an Optional containing the found soft-deleted Address, or empty if not found
     */
    @Query("""
        select a from Address a
        where a.user.id = :userId
          and a.isDeleted = true
          and a.shippingInfo.phone = :phone
          and lower(a.shippingInfo.country) = lower(:country)
          and lower(a.shippingInfo.city) = lower(:city)
          and lower(a.shippingInfo.district) = lower(:district)
          and lower(a.shippingInfo.ward) = lower(:ward)
          and lower(a.shippingInfo.street) = lower(:street)
        """)
    Optional<Address> findSoftDeletedDuplicate(Long userId, String phone, String country, String city, String district, String ward, String street);
}
