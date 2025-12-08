/*
 * @ {#} Payment.java   1.0     8/17/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * @description: Entity class representing a payment in the system.
 * @author: Nguyen Tan Thai Duong
 * @date:   8/17/2025
 * @version:    1.0
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payment_status", columnList = "payment_status"),
                @Index(name = "idx_payment_order_id", columnList = "order_id"),
                @Index(name = "idx_payment_transaction_id", columnList = "transaction_id"),
                @Index(name = "idx_payment_paypal_payment_id", columnList = "paypal_payment_id")
        })
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "paypal_payment_id")
    private String paypalPaymentId;

    @Column(name = "approval_url")
    private String approvalUrl;

    @Column(name = "paypal_capture_id")
    private String paypalCaptureId;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public boolean isCompleted() {
        return PaymentStatus.COMPLETED.equals(this.paymentStatus);
    }
}

