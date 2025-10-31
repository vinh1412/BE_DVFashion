/*
 * @ {#} GhnServiceImpl.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.config.GhnConfig;
import vn.edu.iuh.fit.dtos.request.*;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.services.GhnService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/*
 * @description: Implementation of GHN service integration
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GhnServiceImpl implements GhnService {
    private final RestTemplate restTemplate;
    private final GhnConfig ghnConfig;

    @PostConstruct
    public void init() {
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Token", ghnConfig.getToken());
            request.getHeaders().add("ShopId", ghnConfig.getShopId().toString());
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return execution.execute(request, body);
        });
    }

    @Override
    public BigDecimal calculateShippingFee(CalculateShippingRequest request) {
        try {
            // Prepare GHN request payload
            GhnCalculateShippingRequest ghnRequest = GhnCalculateShippingRequest.builder()
                    .service_id(request.serviceId())
                    .service_type_id(request.serviceTypeId())
                    .to_ward_code(request.toWardCode())
                    .to_district_id(request.toDistrictId())
                    .from_district_id(request.fromDistrictId())
                    .weight(request.weight())
                    .length(request.length())
                    .width(request.width())
                    .height(request.height())
                    .build();

            // Call GHN API to calculate shipping fee
            String url = ghnConfig.getBaseUrl() + "/v2/shipping-order/fee";

            ResponseEntity<GhnCalculateShippingResponse> response = restTemplate.postForEntity(
                    url, ghnRequest, GhnCalculateShippingResponse.class);

            if (response.getBody() != null && response.getBody().code() == 200) {
                return BigDecimal.valueOf(response.getBody().data().total());
            } else {
                log.error("GHN shipping fee calculation failed: {}",
                        response.getBody() != null ? response.getBody().message() : "Unknown error");
                throw new RuntimeException("Failed to calculate shipping fee");
            }
        } catch (Exception e) {
            log.error("Error calculating shipping fee with GHN: {}", e.getMessage());
            throw new RuntimeException("Error calculating shipping fee", e);
        }
    }

    @Override
    public LocalDateTime calculateLeadTime(CalculateLeadTimeRequest request) {
        try {
            // Prepare GHN request payload
            GhnLeadTimeRequest ghnRequest = new GhnLeadTimeRequest(
                    request.fromDistrictId(),
                    request.fromWardCode(),
                    request.toDistrictId(),
                    request.toWardCode(),
                    request.serviceId()
            );

            // Call GHN API to calculate lead time
            String url = ghnConfig.getBaseUrl() + "/v2/shipping-order/leadtime";

            ResponseEntity<GhnLeadTimeResponse> response = restTemplate.postForEntity(
                    url, ghnRequest, GhnLeadTimeResponse.class);

            if (response.getBody() != null && response.getBody().code() == 200) {
                // Parse leadtime (format: timestamp in seconds)
                long leadTimeSeconds = Long.parseLong(response.getBody().data().leadtime());
                log.info("GHN lead time in seconds: {}", leadTimeSeconds);
                Instant instant = Instant.ofEpochSecond(leadTimeSeconds);
                ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
                LocalDateTime timeStamp = LocalDateTime.ofInstant(instant, vietnamZone);
                log.info("GHN lead time as LocalDateTime: {}", timeStamp);
                return timeStamp;
            } else {
                log.error("GHN lead time calculation failed: {}",
                        response.getBody() != null ? response.getBody().message() : "Unknown error");
                throw new RuntimeException("Failed to calculate lead time");
            }
        } catch (Exception e) {
            log.error("Error calculating lead time with GHN: {}", e.getMessage());
            throw new RuntimeException("Error calculating lead time", e);
        }
    }

    @Override
    public Integer getAvailableServiceId(Integer fromDistrict, Integer toDistrict) {
        try {
            String url = ghnConfig.getBaseUrl() + "/v2/shipping-order/available-services";

            Map<String, Object> requestBody = Map.of(
                    "shop_id", Integer.parseInt(ghnConfig.getShopId().toString()),
                    "from_district", fromDistrict,
                    "to_district", toDistrict
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
                if (data != null && !data.isEmpty()) {
                    // Return the first available service ID
                    return (Integer) data.get(0).get("service_id");
                }
            }
            throw new RuntimeException("No available service found for route");
        } catch (Exception e) {
            log.error("Error fetching available services: {}", e.getMessage());
            throw new RuntimeException("Error fetching available services", e);
        }
    }

    @Override
    public List<ProvinceResponse> getProvinces() {
        try {
            String url = ghnConfig.getBaseUrl() + "/master-data/province";

            ResponseEntity<GhnProvincesApiResponse> response = restTemplate.getForEntity(
                    url, GhnProvincesApiResponse.class);

            if (response.getBody() != null && response.getBody().code() == 200) {
                List<ProvinceResponse> provinces = response.getBody().data().stream()
                        .filter(province -> province.provinceId() != 2002) // Remove provinceId 2002
                        .filter(province -> province.nameExtension() != null && !province.nameExtension().isEmpty())
                        .map(province -> new ProvinceResponse(
                                province.provinceId(),
                                province.nameExtension().size() > 1 ?
                                        province.nameExtension().get(1) :
                                        province.nameExtension().get(0)
                        ))
                        .toList();
                log.info("Fetched {} provinces from GHN", provinces.size());
                return provinces;
            } else {
                log.error("GHN get provinces failed: {}",
                        response.getBody() != null ? response.getBody().message() : "Unknown error");
                throw new RuntimeException("Failed to get provinces");
            }
        } catch (Exception e) {
            log.error("Error getting provinces from GHN: {}", e.getMessage());
            throw new RuntimeException("Error getting provinces", e);
        }
    }

    @Override
    public List<DistrictResponse> getDistricts(Integer provinceId) {
        try {
            String url = ghnConfig.getBaseUrl() + "/master-data/district";

            GetDistrictsRequest request = new GetDistrictsRequest(provinceId);

            ResponseEntity<GhnDistrictsApiResponse> response = restTemplate.postForEntity(
                    url, request, GhnDistrictsApiResponse.class);

            if (response.getBody() != null && response.getBody().code() == 200) {
                List<DistrictResponse> districts = response.getBody().data().stream()
                        .map(district -> new DistrictResponse(
                                district.districtId(),
                                district.provinceId(),
                                district.districtName(),
                                district.code()
                        ))
                        .toList();
                log.info("Fetched {} districts for province {} from GHN", districts.size(), provinceId);
                return districts;
            } else {
                log.error("GHN get districts failed: {}",
                        response.getBody() != null ? response.getBody().message() : "Unknown error");
                throw new RuntimeException("Failed to get districts");
            }
        } catch (Exception e) {
            log.error("Error getting districts from GHN: {}", e.getMessage());
            throw new RuntimeException("Error getting districts", e);
        }
    }

    @Override
    public List<WardResponse> getWards(Integer districtId) {
        try {
            String url = ghnConfig.getBaseUrl() + "/master-data/ward";

            GetWardsRequest request = new GetWardsRequest(districtId);

            ResponseEntity<GhnWardsApiResponse> response = restTemplate.postForEntity(
                    url, request, GhnWardsApiResponse.class);

            if (response.getBody() != null && response.getBody().code() == 200) {
                List<WardResponse> wards = response.getBody().data().stream()
                        .map(ward -> new WardResponse(
                                ward.wardCode(),
                                ward.districtId(),
                                ward.wardName()
                        ))
                        .toList();
                log.info("Fetched {} wards for district {} from GHN", wards.size(), districtId);
                return wards;
            } else {
                log.error("GHN get wards failed: {}",
                        response.getBody() != null ? response.getBody().message() : "Unknown error");
                throw new RuntimeException("Failed to get wards");
            }
        } catch (Exception e) {
            log.error("Error getting wards from GHN: {}", e.getMessage());
            throw new RuntimeException("Error getting wards", e);
        }
    }
}
