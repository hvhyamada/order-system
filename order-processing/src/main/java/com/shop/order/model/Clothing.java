package com.shop.order.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Одяг. Об'єкти створюються через патерн Builder (Lombok @Builder).
 */
@Getter
@ToString(callSuper = true)
public class Clothing extends Product {

    private final String size;
    private final String material;

    @Builder
    private Clothing(String id, String name, BigDecimal price, String size, String material) {
        super(id, name, price);
        this.size = size;
        this.material = material;
    }

    @Override
    public String getCategory() {
        return "Одяг";
    }
}
