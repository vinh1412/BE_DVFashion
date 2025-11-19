/*
 * @ {#} OrderController.java   1.0     28/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.constants.RoleConstant;
import vn.edu.iuh.fit.dtos.request.AdminUpdateOrderRequest;
import vn.edu.iuh.fit.dtos.request.CancelOrderRequest;
import vn.edu.iuh.fit.dtos.request.CreateOrderRequest;
import vn.edu.iuh.fit.dtos.request.UpdateOrderByUserRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.OrderResponse;
import vn.edu.iuh.fit.dtos.response.OrderStatisticsResponse;
import vn.edu.iuh.fit.dtos.response.PageResponse;
import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.enums.PaymentStatus;
import vn.edu.iuh.fit.services.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Controller for handling order-related endpoints
 * @author: Tran Hien Vinh
 * @date:   28/09/2025
 * @version:    1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${web.base-path}/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse orderResponse = orderService.createOrder(request);
        return ResponseEntity.ok(ApiResponse.success(orderResponse));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_CUSTOMER)
    @PutMapping("/{orderNumber}/user")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderByUser(
            @PathVariable String orderNumber,
            @Valid @RequestBody UpdateOrderByUserRequest request
    ) {
        OrderResponse response = orderService.updateOrderByUser(orderNumber, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Order updated"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @PutMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> adminUpdateOrder(
            @PathVariable String orderNumber,
            @Valid @RequestBody AdminUpdateOrderRequest request
    ) {
        OrderResponse response = orderService.adminUpdateOrder(orderNumber, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Order updated"));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderResponse orderResponse = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(orderResponse, "Order retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_CUSTOMER)
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders() {
        List<OrderResponse> orders = orderService.getOrdersByCurrentCustomer();
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Customer orders retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_CUSTOMER)
    @GetMapping("/my-orders/paging")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrdersPaging(
            @PageableDefault(page = 0, size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<OrderResponse> orders = orderService.getOrdersByCurrentCustomerPaging(pageable);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_CUSTOMER)
    @GetMapping("/customer/{customerId}/paging")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrdersByCustomerIdPaging(
            @PathVariable Long customerId,
            @PageableDefault(page = 0, size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<OrderResponse> orders = orderService.getOrdersByCustomerIdPaging(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders, "Customer orders retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrdersPaging(
            @PageableDefault(page = 0, size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<OrderResponse> orders = orderService.getAllOrdersPaging(pageable);
        return ResponseEntity.ok(ApiResponse.success(orders, "All orders retrieved"));
    }

//    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
//    @GetMapping("/all")
//    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
//        List<OrderResponse> orders = orderService.getAllOrders();
//        return ResponseEntity.ok(ApiResponse.success(orders, "All orders retrieved"));
//    }

    @PreAuthorize(RoleConstant.HAS_ROLE_CUSTOMER)
    @PutMapping("/{orderNumber}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable String orderNumber,
            @Valid @RequestBody CancelOrderRequest request) {

        OrderResponse response = orderService.cancelOrderByCustomer(orderNumber, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Order cancelled successfully"));
    }

    @GetMapping("/statistics")
    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<OrderStatisticsResponse>> getOrderStatistics() {
        OrderStatisticsResponse statistics = orderService.getOrderStatistics();

        return ResponseEntity.ok(ApiResponse.success(statistics, "Order statistics fetched successfully"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/all/pending")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getPendingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate
    ) {
        PageResponse<OrderResponse> orders = orderService.getPendingOrders(
                page, size, sort, search, paymentMethod, paymentStatus,
                customerId, minTotal, maxTotal, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success(orders, "Pending orders retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/all/confirmed")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getConfirmedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate
    ) {
        PageResponse<OrderResponse> orders = orderService.getConfirmedOrders(
                page, size, sort, search, paymentMethod, paymentStatus,
                customerId, minTotal, maxTotal, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success(orders, "Confirmed orders retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/all/processing")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getProcessingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate
    ) {
        PageResponse<OrderResponse> orders = orderService.getProcessingOrders(
                page, size, sort, search, paymentMethod, paymentStatus,
                customerId, minTotal, maxTotal, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success(orders, "Processing orders retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/all/shipped")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getShippedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate
    ) {
        PageResponse<OrderResponse> orders = orderService.getShippedOrders(
                page, size, sort, search, paymentMethod, paymentStatus,
                customerId, minTotal, maxTotal, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success(orders, "Shipped orders retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/all/delivered")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getDeliveredOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate
    ) {
        PageResponse<OrderResponse> orders = orderService.getDeliveredOrders(
                page, size, sort, search, paymentMethod, paymentStatus,
                customerId, minTotal, maxTotal, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success(orders, "Delivered orders retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/all/cancelled")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getCancelledOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate
    ) {
        PageResponse<OrderResponse> orders = orderService.getCancelledOrders(
                page, size, sort, search, paymentMethod, paymentStatus,
                customerId, minTotal, maxTotal, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success(orders, "Cancelled orders retrieved"));
    }

    @PreAuthorize(RoleConstant.HAS_ROLE_ADMIN)
    @GetMapping("/all/returned")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getReturnedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate
    ) {
        PageResponse<OrderResponse> orders = orderService.getReturnedOrders(
                page, size, sort, search, paymentMethod, paymentStatus,
                customerId, minTotal, maxTotal, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success(orders, "Returned orders retrieved"));
    }
}
