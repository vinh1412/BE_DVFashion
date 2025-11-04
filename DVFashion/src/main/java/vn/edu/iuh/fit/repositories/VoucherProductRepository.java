/*
 * @ {#} VoucherProductRepository.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.VoucherProduct;

/*
 * @description: Repository interface for managing VoucherProduct entities.
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
@Repository
public interface VoucherProductRepository extends JpaRepository<VoucherProduct, Long> {
}
