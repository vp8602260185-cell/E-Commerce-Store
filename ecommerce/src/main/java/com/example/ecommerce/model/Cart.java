package com.example.ecommerce.model;

import java.util.concurrent.ConcurrentHashMap;

public class Cart {
    // Maps ProductId to Quantity
    private final ConcurrentHashMap<String, Integer> items = new ConcurrentHashMap<>();

    public void addItem(String productId, int quantity) {
        items.merge(productId, quantity, Integer::sum);
    }

    public ConcurrentHashMap<String, Integer> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }
}