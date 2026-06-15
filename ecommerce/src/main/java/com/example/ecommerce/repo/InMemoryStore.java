package com.example.ecommerce.repo;

import com.example.ecommerce.model.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Repository
public class InMemoryStore {

    // In-memory catalogs and tracking structures
    private final Map<String, Product> productCatalog = new ConcurrentHashMap<>();
    private final Map<String, Cart> userCarts = new ConcurrentHashMap<>();
    private final Map<String, Coupon> coupons = new ConcurrentHashMap<>();
    
    // Global Order Counter for the "Every Nth order gets a coupon" rule
    private final AtomicLong orderCounter = new AtomicLong(0);

    // Admin Metrics accumulators
    private final AtomicLong totalItemsPurchased = new AtomicLong(0);
    private final AtomicReference<BigDecimal> totalRevenue = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> totalDiscountsGiven = new AtomicReference<>(BigDecimal.ZERO);

    public InMemoryStore() {
        productCatalog.put("P1", new Product("P1", "Laptop", new BigDecimal("50.00")));
        productCatalog.put("P2", new Product("P2", "Smartphone", new BigDecimal("50.00")));
        productCatalog.put("P3", new Product("P3", "Headphones", new BigDecimal("50.00")));
    }

    public Product getProduct(String id) { return productCatalog.get(id); }
    public Cart getOrCreateCart(String userId) { return userCarts.computeIfAbsent(userId, k -> new Cart()); }
    public Coupon getCoupon(String code) { return coupons.get(code); }
    public void saveCoupon(Coupon coupon) { coupons.put(coupon.code(), coupon); }
    public Map<String, Coupon> getAllCoupons() { return coupons; }
    public long incrementAndGetOrderCount() { return orderCounter.incrementAndGet(); }

    // Thread-safe update of admin metrics
    public void updateMetrics(long itemsCount, BigDecimal revenue, BigDecimal discount) {
        totalItemsPurchased.addAndGet(itemsCount);
        totalRevenue.updateAndGet(current -> current.add(revenue));
        totalDiscountsGiven.updateAndGet(current -> current.add(discount));
    }

    public OrderSummary getSummary() {
        return new OrderSummary(
            totalItemsPurchased.get(),
            totalRevenue.get(),
            coupons.keySet().stream().toList(),
            totalDiscountsGiven.get()
        );
    }
}