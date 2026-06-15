package com.example.ecommerce.service;

import com.example.ecommerce.dto.CheckoutResult;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repo.InMemoryStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final InMemoryStore store;

    // Configurable business constraints (Can be moved to application.properties)
    private static final int NTH_ORDER = 3; 
    private static final BigDecimal DISCOUNT_PCT = new BigDecimal("10.00"); // 10%

    public OrderService(InMemoryStore store) {
        this.store = store;
    }

    public void addToCart(String userId, CartItem item) {
        if (store.getProduct(item.productId()) == null) {
            throw new IllegalArgumentException("Product not found");
        }
        Cart cart = store.getOrCreateCart(userId);
        cart.addItem(item.productId(), item.quantity());
    }

    public CheckoutResult checkout(String userId, String couponCode) {
        Cart cart = store.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        long totalItems = 0;

        // Calculate Cart Totals
        for (Map.Entry<String, Integer> entry : cart.getItems().entrySet()) {
            Product product = store.getProduct(entry.getKey());
            BigDecimal quantity = BigDecimal.valueOf(entry.getValue());
            subtotal = subtotal.add(product.price().multiply(quantity));
            totalItems += entry.getValue();
        }

        // Validate and apply coupon if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            Coupon coupon = store.getCoupon(couponCode);
            if (coupon == null || coupon.isUsed()) {
                throw new IllegalArgumentException("Invalid or already used coupon code");
            }
            BigDecimal discountMultiplier = coupon.discountPercentage().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            discountAmount = subtotal.multiply(discountMultiplier);
            
            // Mark coupon as used
            store.saveCoupon(coupon.use());
        }

        BigDecimal netTotal = subtotal.subtract(discountAmount);

        // Increment order counter and check if this order qualifies for an Nth order coupon reward
        long orderSequence = store.incrementAndGetOrderCount();
        String rewardCouponCode = null;
        if (orderSequence % NTH_ORDER == 0) {
            rewardCouponCode = "REWARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            store.saveCoupon(new Coupon(rewardCouponCode, DISCOUNT_PCT, false));
        }

        // Update Global metrics
        store.updateMetrics(totalItems, netTotal, discountAmount);

        // Clear cart after a successful checkout
        cart.clear();

        return new CheckoutResult(subtotal, discountAmount, netTotal, rewardCouponCode);
        }

    public String generateManualCoupon() {
        String code = "ADMIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        store.saveCoupon(new Coupon(code, DISCOUNT_PCT, false));
        return code;
    }

    public OrderSummary getAdminMetrics() {
        return store.getSummary();
    }
}