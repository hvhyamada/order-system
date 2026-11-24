package com.shop.order.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Замовлення на товар типу T. Параметр обмежений: T extends Product.
 */
@Getter
@ToString
public class Order<T extends Product> {

    private final String id;
    private final T product;
    private final int quantity;
    private final String customer;
    private volatile OrderStatus status;
    private volatile BigDecimal total;
    private volatile String processedBy;

    @Builder
    private Order(String id, T product, int quantity, String customer) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.customer = customer;
        this.status = OrderStatus.NEW;
    }

    /** Позначає замовлення як оброблене (викликається обробником). */
    public void markProcessed(BigDecimal total, String threadName) {
        this.total = total;
        this.processedBy = threadName;
        this.status = OrderStatus.PROCESSED;
    }

    /** Позначає замовлення як невдале. */
    public void markFailed(String threadName) {
        this.processedBy = threadName;
        this.status = OrderStatus.FAILED;
    }
}
