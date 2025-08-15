/*
 * @ {#} RoleConstant.java   1.0     16/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.constants;

/*
 * @description: This class defines constants for user roles in the application
 * @author: Tran Hien Vinh
 * @date:   16/08/2025
 * @version:    1.0
 */
public class RoleConstant {
    public static final String ADMIN = "ADMIN";
    public static final String STAFF = "STAFF";
    public static final String CUSTOMER = "CUSTOMER";

    public static final String HAS_ROLE_ADMIN = "hasRole('" + ADMIN + "')";
    public static final String HAS_ROLE_STAFF = "hasRole('" + STAFF + "')";
    public static final String HAS_ROLE_CUSTOMER = "hasRole('" + CUSTOMER + "')";

    public static final String HAS_ANY_ROLE_ADMIN_STAFF = "hasAnyRole('" + ADMIN + "','" + STAFF + "')";
    public static final String HAS_ANY_ROLE_ADMIN_STAFF_CUSTOMER =
            "hasAnyRole('" + ADMIN + "','" + STAFF + "','" + CUSTOMER + "')";
}
