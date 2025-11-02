/*
 * @ {#} Voucher.java   1.0     02/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.enums.DiscountType;
import vn.edu.iuh.fit.enums.VoucherType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   02/11/2025
 * @version:    1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private VoucherType type; // SHOP_WIDE or PRODUCT_SPECIFIC

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "allow_pre_save", nullable = false)
    private Boolean allowPreSave; // Allow saving voucher before activation

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType; // PERCENTAGE or FIXED_AMOUNT

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "has_max_discount", nullable = false)
    private Boolean hasMaxDiscount; // Has maximum discount limit

    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount; // Maximum discount (if hasMaxDiscount is true)

    @Column(name = "min_order_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "max_total_usage", nullable = false)
    private Integer maxTotalUsage; // Maximum total usage

    @Column(name = "current_usage", nullable = false)
    private Integer currentUsage;

    @Column(name = "max_usage_per_user", nullable = false)
    private Integer maxUsagePerUser;

    @Column(nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoucherTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoucherProduct> voucherProducts = new ArrayList<>();

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoucherUsage> voucherUsages = new ArrayList<>();

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active &&
                now.isAfter(startDate) &&
                now.isBefore(endDate) &&
                currentUsage < maxTotalUsage;
    }

    public boolean canUserUse(Long userId) {
        if (!isValid()) return false;

        long userUsageCount = voucherUsages.stream()
                .filter(usage -> usage.getUser().getId().equals(userId))
                .count();

        return userUsageCount < maxUsagePerUser;
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (orderAmount.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = orderAmount.multiply(discountValue.divide(BigDecimal.valueOf(100)));
        } else {
            discount = discountValue;
        }

        // Áp dụng giới hạn mức giảm tối đa nếu có
        if (hasMaxDiscount && maxDiscountAmount != null) {
            discount = discount.min(maxDiscountAmount);
        }

        return discount;
    }
}
