/*
 * @ {#} OpenApiConfig.java   1.0     25/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
 * @description: Configuration class for OpenAPI documentation
 * @author: Tran Hien Vinh
 * @date:   25/09/2025
 * @version:    1.0
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DVFashion API")
                        .version("1.0")
                        .description("API documentation for the DVFashion"))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Local server")));
//                .components(new Components().addSecuritySchemes("bearerAuth",
//                        new SecurityScheme()
//                                .type(SecurityScheme.Type.HTTP)
//                                .scheme("bearer")
//                                .bearerFormat("JWT")));
    }
}
