package com.example.ecommerce.controller;

import com.example.ecommerce.model.OrderSummary;
import com.example.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OrderService orderService;

    public AdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/coupon/generate")
    public ResponseEntity<String> generateCoupon() {
        String couponCode = orderService.generateManualCoupon();
        return ResponseEntity.ok("Coupon Generated: " + couponCode);
    }

    @GetMapping("/metrics")
    public ResponseEntity<OrderSummary> getMetrics() {
        return ResponseEntity.ok(orderService.getAdminMetrics());
    }
}