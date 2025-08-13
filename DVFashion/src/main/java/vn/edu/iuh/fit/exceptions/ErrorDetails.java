/*
 * @ {#} ErrorDetails.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {
    // Error code
    private String code;

    // Error message
    private String message;

    // Detailed description
    private String details;

    // Field-specific validation errors
    private List<ValidationError> validationErrors;
}
