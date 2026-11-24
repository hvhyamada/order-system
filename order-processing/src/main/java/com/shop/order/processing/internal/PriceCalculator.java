package com.shop.order.processing.internal;

import com.shop.order.model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Внутрішній клас модуля order.processing.
 * Клас публічний, але пакет не експортується в module-info.java,
 * тому з інших модулів він недоступний (компілятор видасть помилку).
 */
public final class PriceCalculator {

    private static final int BULK_QUANTITY = 5;
    private static final BigDecimal BULK_DISCOUNT = new BigDecimal("0.10");

    private PriceCalculator() {
    }

    /** Сума замовлення зі знижкою 10% від 5 одиниць товару. */
    public static <T extends Product> BigDecimal total(T product, int quantity) {
        BigDecimal sum = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        if (quantity >= BULK_QUANTITY) {
            sum = sum.subtract(sum.multiply(BULK_DISCOUNT));
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }
}
