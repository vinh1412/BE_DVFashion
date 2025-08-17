/*
 * @ {#} TestController.java   1.0     17/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   17/08/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.constants.RoleConstant;

@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/test")
public class TestController {
     @GetMapping("/example")
     public ResponseEntity<String> exampleEndpoint() {
         return ResponseEntity.ok("This is an example endpoint.");
     }

     @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_STAFF_CUSTOMER)
        @GetMapping("/admin-staff-customer")
        public ResponseEntity<String> adminStaffCustomerEndpoint() {
         return ResponseEntity.ok("This endpoint is accessible by Admin, Staff, and Customer roles.");
     }

        @PreAuthorize(RoleConstant.HAS_ANY_ROLE_ADMIN_STAFF)
            @GetMapping("/admin-staff")
            public ResponseEntity<String> adminStaffEndpoint() {
            return ResponseEntity.ok("This endpoint is accessible by Admin and Staff roles.");
        }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/admin")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("This endpoint is accessible by Admin role.");
    }
}
