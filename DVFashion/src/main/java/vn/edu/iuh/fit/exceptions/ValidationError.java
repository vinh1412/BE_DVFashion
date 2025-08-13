/*
 * @ {#} ValidationError.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationError {
    // Field name that failed validation
    private String field;

    // Rejected value
    private Object rejectedValue;

    // Validation error message
    private String message;

    // Error code for this specific validation
    private String code;
}
