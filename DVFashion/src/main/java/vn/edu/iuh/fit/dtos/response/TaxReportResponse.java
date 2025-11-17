/*
 * @ {#} TaxReportResponse.java   1.0     16/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

/*
 * @description: Response DTO for tax reports
 * @author: Tran Hien Vinh
 * @date:   16/11/2025
 * @version:    1.0
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaxReportResponse(
        VATSalesListReport vatSalesListReport
) {}
