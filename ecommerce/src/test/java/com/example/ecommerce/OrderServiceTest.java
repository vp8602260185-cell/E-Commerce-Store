package com.example.ecommerce;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.dto.CheckoutResult;
import com.example.ecommerce.model.OrderSummary;
import com.example.ecommerce.repo.InMemoryStore;
import com.example.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private InMemoryStore store;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        store = new InMemoryStore();
        orderService = new OrderService(store);
    }

    @Test
    void testAddToCartAndCheckoutWithoutCoupon() {
        // Add a Wireless Mouse ($25.00) x 2 = $50.00
        orderService.addToCart("user1", new CartItem("P1", 1)); 
        
        // Execute checkout
        CheckoutResult result = orderService.checkout("user1", null);
        
        // Assertions on the receipt (CheckoutResult)
        assertEquals(new BigDecimal("50.00"), result.subtotal());
        assertEquals(BigDecimal.ZERO, result.discountApplied());
        assertEquals(new BigDecimal("50.00"), result.netTotal());
        assertNull(result.rewardCouponCode()); // 1st order should NOT get a coupon
    }

    @Test
    void testNthOrderGeneratesAndReturnsCoupon() {
        // --- Order 1 ---
        orderService.addToCart("user1", new CartItem("P1", 1));
        CheckoutResult result1 = orderService.checkout("user1", null);
        assertNull(result1.rewardCouponCode()); // Order #1: No coupon

        // --- Order 2 ---
        orderService.addToCart("user2", new CartItem("P1", 1));
        CheckoutResult result2 = orderService.checkout("user2", null);
        assertNull(result2.rewardCouponCode()); // Order #2: No coupon

        // --- Order 3 (The Nth Order) ---
        orderService.addToCart("user3", new CartItem("P1", 1));
        CheckoutResult result3 = orderService.checkout("user3", null);

        // Assertions for the 3rd order reward
        assertNotNull(result3.rewardCouponCode());
        assertTrue(result3.rewardCouponCode().startsWith("REWARD-"));

        // Verify the newly generated coupon actually works by using it for Order 4
        orderService.addToCart("user4", new CartItem("P2", 1)); // Keyboard ($75.00)
        CheckoutResult result4 = orderService.checkout("user4", result3.rewardCouponCode());

        // Assertions for coupon application (10% off of $50.00 = $5 discount)
        assertEquals(new BigDecimal("50.00"), result4.subtotal());
        assertEquals(new BigDecimal("5.0000"), result4.discountApplied());
        assertEquals(new BigDecimal("45.0000"), result4.netTotal());
    }

    @Test
    void testInvalidCouponThrowsException() {
        orderService.addToCart("user1", new CartItem("P1", 1));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.checkout("user1", "FAKE_COUPON_CODE");
        });

        assertEquals("Invalid or already used coupon code", exception.getMessage());
    }

    @Test
    void testAdminMetricsStillAccumulateCorrectly() {
        // Order 1: $50.00
        orderService.addToCart("user1", new CartItem("P1", 1));
        orderService.checkout("user1", null);

        // Order 2: $50.00
        orderService.addToCart("user2", new CartItem("P2", 1));
        orderService.checkout("user2", null);

        // Check global Admin Metrics separate from checkout responses
        OrderSummary metrics = orderService.getAdminMetrics();
        
        assertEquals(2, metrics.totalItemsPurchased()); // 1 mouse + 1 keyboard
        assertEquals(new BigDecimal("100.00"), metrics.totalRevenue()); // 
        assertEquals(BigDecimal.ZERO, metrics.totalDiscountsGiven());
    }
}