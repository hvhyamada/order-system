package com.shop.order.processing;

import com.shop.order.model.Order;
import com.shop.order.model.Product;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Фільтрація замовлень за допомогою лямбда-виразів і узагальнених методів.
 */
public final class OrderFilters {

    private OrderFilters() {
    }

    /**
     * Вибирає замовлення з товарами потрібного типу і повертає їх уже як Order&lt;T&gt;.
     */
    public static <T extends Product> List<Order<T>> byType(
            Collection<? extends Order<? extends Product>> orders, Class<T> type) {
        return orders.stream()
                .filter(order -> type.isInstance(order.getProduct()))
                .map(order -> OrderFilters.<T>cast(order))
                .toList();
    }

    /** Загальний фільтр за довільною умовою. */
    public static <T extends Product> List<Order<T>> where(
            Collection<Order<T>> orders, Predicate<? super Order<T>> condition) {
        return orders.stream().filter(condition).toList();
    }

    /**
     * Безпечне приведення товару до типу T.
     *
     * @throws InvalidProductTypeException якщо товар має інший тип
     */
    public static <T extends Product> T requireType(Product product, Class<T> type) {
        if (!type.isInstance(product)) {
            throw new InvalidProductTypeException(type, product == null ? null : product.getClass());
        }
        return type.cast(product);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Product> Order<T> cast(Order<? extends Product> order) {
        // Безпечно: тип товару перевірено у filter перед викликом.
        return (Order<T>) order;
    }
}
