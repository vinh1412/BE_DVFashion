/*
 * @ {#} RecommendationModelVersionMapper.java   1.0     26/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.mappers;

import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.dtos.response.RecommendationModelVersionResponse;
import vn.edu.iuh.fit.entities.RecommendationModelVersion;

/*
 * @description: Mapper for RecommendationModelVersion entity to RecommendationModelVersionResponse DTO
 * @author: Tran Hien Vinh
 * @date:   26/10/2025
 * @version:    1.0
 */
@Component
public class RecommendationModelVersionMapper {

    public RecommendationModelVersionResponse toResponse(RecommendationModelVersion entity) {
        return new RecommendationModelVersionResponse(
                entity.getId(),
                entity.getModelName(),
                entity.getContentWeight(),
                entity.getCollaborativeWeight(),
                entity.getPrecisionAt10(),
                entity.getRecallAt10(),
                entity.getMapAt10(),
                entity.getIsActive(),
                entity.getCreatedAt()
        );
    }
}
