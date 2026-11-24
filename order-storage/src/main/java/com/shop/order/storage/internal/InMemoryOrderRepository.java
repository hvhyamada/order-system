package com.shop.order.storage.internal;

import com.shop.order.model.Order;
import com.shop.order.model.Product;
import com.shop.order.processing.InvalidProductTypeException;
import com.shop.order.storage.OrderRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Потокобезпечна реалізація сховища на ConcurrentHashMap.
 * Пакет не експортується — клас недоступний поза модулем order.storage.
 */
public class InMemoryOrderRepository<T extends Product> implements OrderRepository<T> {

    private final Class<T> productType;
    private final Map<String, Order<T>> orders = new ConcurrentHashMap<>();

    public InMemoryOrderRepository(Class<T> productType) {
        this.productType = productType;
    }

    @Override
    public void save(Order<T> order) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Замовлення або його id відсутні");
        }
        // Захист від "забруднення купи" через сирі типи.
        if (!productType.isInstance(order.getProduct())) {
            throw new InvalidProductTypeException(productType, order.getProduct().getClass());
        }
        orders.put(order.getId(), order);
    }

    @Override
    public Optional<Order<T>> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order<T>> findAll() {
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::getId))
                .toList();
    }

    @Override
    public List<Order<T>> findBy(Predicate<? super Order<T>> condition) {
        return findAll().stream().filter(condition).toList();
    }

    @Override
    public int count() {
        return orders.size();
    }
}
