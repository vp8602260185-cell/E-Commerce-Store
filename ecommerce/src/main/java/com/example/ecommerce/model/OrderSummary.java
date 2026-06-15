package com.example.ecommerce.model;

import java.math.BigDecimal;
import java.util.List;

public record OrderSummary(
    long totalItemsPurchased,
    BigDecimal totalRevenue,
    List<String> discountCodes,
    BigDecimal totalDiscountsGiven
) {}