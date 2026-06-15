package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CheckoutResult;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.OrderSummary;
import com.example.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final OrderService orderService;

    public CartController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<String> addToCart(@PathVariable String userId, @RequestBody CartItem item) {
        try {
            orderService.addToCart(userId, item);
            return ResponseEntity.ok("Item added to cart successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<?> checkout(@PathVariable String userId, @RequestParam(required = false) String couponCode) {
        try {
            CheckoutResult result = orderService.checkout(userId, couponCode);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}