/*
 * @ {#} PaymentRepository.java   1.0     06/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.Payment;

/*
 * @description: Repository interface for payment data access
 * @author: Tran Hien Vinh
 * @date:   06/10/2025
 * @version:    1.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
