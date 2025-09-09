/*
 * @ {#} StockTransactionRepository.java   1.0     09/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.StockTransaction;

/*
 * @description: Repository for managing stock transactions
 * @author: Tran Hien Vinh
 * @date:   09/09/2025
 * @version:    1.0
 */
@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
}
