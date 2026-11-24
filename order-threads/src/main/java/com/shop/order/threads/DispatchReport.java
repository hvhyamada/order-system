package com.shop.order.threads;

import java.util.List;

/**
 * Підсумок паралельної обробки однієї групи замовлень.
 */
public record DispatchReport(String category, int processed, int failed,
                             List<String> errors, long millis) {
}
