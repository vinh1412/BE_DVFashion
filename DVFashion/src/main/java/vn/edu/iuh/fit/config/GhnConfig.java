/*
 * @ {#} GhnConfig.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
 * @description: Configuration class for GHN (Giao Hàng Nhanh) API properties
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
@Configuration
@ConfigurationProperties(prefix = "ghn")
@Data
public class GhnConfig {
    private String baseUrl;
    private String token;
    private Integer shopId;
    private String shopName;
}
