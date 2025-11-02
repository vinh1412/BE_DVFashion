/*
 * @ {#} VoucherRepository.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Voucher;

/*
 * @description: Repository interface for Voucher entity
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    /**
     * Check if a voucher exists by its code, ignoring case sensitivity.
     *
     * @param code the voucher code to check
     * @return true if a voucher with the given code exists, false otherwise
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Find a voucher by its code, ignoring case sensitivity.
     *
     * @param code the voucher code to search for
     * @return the Voucher entity if found, null otherwise
     */
    @Query("SELECT v FROM Voucher v WHERE UPPER(v.code) = UPPER(:code)")
    Voucher findByCodeIgnoreCase(@Param("code") String code);
}
