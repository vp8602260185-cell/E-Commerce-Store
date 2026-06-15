package com.example.ecommerce.dto;

import java.math.BigDecimal;

public record CheckoutResult(
    BigDecimal subtotal,
    BigDecimal discountApplied,
    BigDecimal netTotal,
    String rewardCouponCode // Will contain the code if this was the Nth order!
) {}