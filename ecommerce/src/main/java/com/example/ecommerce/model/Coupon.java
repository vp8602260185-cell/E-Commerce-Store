package com.example.ecommerce.model;

import java.math.BigDecimal;

public record Coupon(String code, BigDecimal discountPercentage, boolean isUsed) {
    public Coupon use() {
        return new Coupon(this.code, this.discountPercentage, true);
    }
}