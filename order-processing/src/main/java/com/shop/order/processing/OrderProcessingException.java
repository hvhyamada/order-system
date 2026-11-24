package com.shop.order.processing;

/**
 * Перевіряєме виключення: замовлення не вдалося обробити
 * (некоректна кількість, переривання потоку тощо).
 */
public class OrderProcessingException extends Exception {

    public OrderProcessingException(String message) {
        super(message);
    }

    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
