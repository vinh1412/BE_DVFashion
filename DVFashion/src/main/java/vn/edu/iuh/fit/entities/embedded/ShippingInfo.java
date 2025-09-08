/*
 * @ {#} ShippingInfo.java   1.0     08/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities.embedded;

import jakarta.persistence.Embeddable;
import lombok.*;

/*
 * @description: Embedded class representing shipping information for an order or address.
 * @author: Tran Hien Vinh
 * @date:   08/09/2025
 * @version:    1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class ShippingInfo {
    private String fullName;

    private String phone;

    private String country;

    private String city;

    private String district;

    private String ward;

    private String street;
}
