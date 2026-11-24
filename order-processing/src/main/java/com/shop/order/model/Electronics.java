package com.shop.order.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Електроніка. Об'єкти створюються через патерн Builder (Lombok @Builder).
 */
@Getter
@ToString(callSuper = true)
public class Electronics extends Product {

    private final String brand;
    private final int warrantyMonths;

    @Builder
    private Electronics(String id, String name, BigDecimal price, String brand, int warrantyMonths) {
        super(id, name, price);
        if (warrantyMonths < 0) {
            throw new IllegalArgumentException("Гарантія не може бути від'ємною");
        }
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getCategory() {
        return "Електроніка";
    }
}
