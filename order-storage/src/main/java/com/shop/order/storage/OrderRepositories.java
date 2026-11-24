package com.shop.order.storage;

import com.shop.order.model.Product;
import com.shop.order.storage.internal.InMemoryOrderRepository;

/**
 * Фабрика сховищ. Інші модулі отримують сховище лише через неї,
 * бо клас реалізації знаходиться в неекспортованому пакеті.
 */
public final class OrderRepositories {

    private OrderRepositories() {
    }

    /** Потокобезпечне сховище в пам'яті для товарів типу T. */
    public static <T extends Product> OrderRepository<T> inMemory(Class<T> productType) {
        return new InMemoryOrderRepository<>(productType);
    }
}
