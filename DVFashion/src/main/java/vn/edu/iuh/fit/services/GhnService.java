/*
 * @ {#} GhnService.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.*;
import vn.edu.iuh.fit.dtos.response.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Service interface for GHN integration
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
public interface GhnService {
    /**
     * Calculate shipping fee based on the provided request details.
     *
     * @param request the shipping calculation request containing necessary details
     * @return the calculated shipping fee as BigDecimal
     */
    BigDecimal calculateShippingFee(CalculateShippingRequest request);

    /**
     * Calculate lead time for delivery based on the provided request details.
     *
     * @param request the lead time calculation request containing necessary details
     * @return the estimated lead time as LocalDateTime
     */
    LocalDateTime calculateLeadTime(CalculateLeadTimeRequest request);

    /**
     * Get available service ID for shipping between two districts.
     *
     * @param fromDistrict the ID of the origin district
     * @param toDistrict   the ID of the destination district
     * @return the available service ID as Integer
     */
    Integer getAvailableServiceId(Integer fromDistrict, Integer toDistrict);

    /**
     * Get list of provinces.
     *
     * @return list of ProvinceResponse
     */
    List<ProvinceResponse> getProvinces();

    /**
     * Get list of districts for a given province ID.
     *
     * @param provinceId the ID of the province
     * @return list of DistrictResponse
     */
    List<DistrictResponse> getDistricts(Integer provinceId);

    /**
     * Get list of wards for a given district ID.
     *
     * @param districtId the ID of the district
     * @return list of WardResponse
     */
    List<WardResponse> getWards(Integer districtId);
}
