/*
 * @ {#} PythonEvaluationResponse.java   1.0     26/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.Data;

/*
 * @description: DTO for Python evaluation response
 * @author: Tran Hien Vinh
 * @date:   26/10/2025
 * @version:    1.0
 */
@Data
public class PythonEvaluationResponse {
    private String message;

    private Metrics metrics;

    @Data
    public static class Metrics {
        private String model_name;

        private Double content_weight;

        private Double collaborative_weight;

        private Double precision_at_10;

        private Double recall_at_10;

        private Double map_at_10;
    }
}
