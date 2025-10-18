/*
 * @ {#} ReviewImage.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.ReviewImage;

/*
 * @description: Repository interface for managing review images in the database.
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
}
