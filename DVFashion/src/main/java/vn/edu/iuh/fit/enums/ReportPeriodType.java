/*
 * @ {#} ReportPeriodType.java   1.0     15/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.enums;

/*
 * @description: Enumeration for report period types
 * @author: Tran Hien Vinh
 * @date:   15/11/2025
 * @version:    1.0
 */
public enum ReportPeriodType {
    DAILY("Ngày", "dd/MM/yyyy"),
    MONTHLY("Tháng", "MM/yyyy"),
    QUARTERLY("Quý", "QQQ yyyy"),
    YEARLY("Năm", "yyyy");

    private final String displayName;
    private final String dateFormat;

    ReportPeriodType(String displayName, String dateFormat) {
        this.displayName = displayName;
        this.dateFormat = dateFormat;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
