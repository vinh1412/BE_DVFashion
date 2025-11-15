/*
 * @ {#} ForecastingServiceImpl.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.dtos.response.RevenueDataPoint;
import vn.edu.iuh.fit.services.ForecastingService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ForecastingServiceImpl implements ForecastingService {
    @Value("#{ '${recommendation.service.url}' + '${web.base-path}' }")
    private String recommendationServiceUrl;

    private final RestTemplate restTemplate;
    @Override
    public List<RevenueDataPoint> getRevenueForecast(int days) {
        String url = recommendationServiceUrl + "/forecast/revenue";
        try {
            Map<String, Integer> requestBody = Map.of("days", days);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestBody, JsonNode.class);

            JsonNode forecastNode = response.getBody().get("forecast");
            List<RevenueDataPoint> forecastList = new ArrayList<>();

            if (forecastNode != null && forecastNode.isArray()) {
                for (JsonNode node : forecastNode) {
                    String date = node.get("ds").asText();
                    BigDecimal revenue = new BigDecimal(node.get("yhat").asDouble());
                    // Bạn có thể thêm yhat_lower và yhat_upper vào DTO nếu muốn
                    forecastList.add(new RevenueDataPoint(date, revenue));
                }
            }
            return forecastList;
        } catch (Exception e) {
            log.error("Error fetching revenue forecast from Python service", e);
            throw new RuntimeException("Could not retrieve forecast: " + e.getMessage());
        }
    }

    @Override
    public void triggerModelRetraining() {
        String url = recommendationServiceUrl + "/forecast/retrain";
        try {
            restTemplate.postForEntity(url, null, String.class);
            log.info("Successfully triggered model retraining.");
        } catch (Exception e) {
            log.error("Failed to trigger model retraining", e);
        }
    }
}
