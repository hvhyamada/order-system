package com.shop.order.model;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Базовий клас товару. Узагальнення системи обмежені цим типом (T extends Product).
 */
@Getter
@ToString
public abstract class Product {

    private final String id;
    private final String name;
    private final BigDecimal price;

    protected Product(String id, String name, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Назва товару не може бути порожньою");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Ціна товару має бути більшою за нуль: " + price);
        }
        this.id = id;
        this.name = name;
        this.price = price;
    }

    /** Категорія товару для виводу та фільтрації. */
    public abstract String getCategory();
}
