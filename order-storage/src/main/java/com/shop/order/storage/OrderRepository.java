package com.shop.order.storage;

import com.shop.order.model.Order;
import com.shop.order.model.OrderStatus;
import com.shop.order.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Сховище замовлень з товарами типу T.
 *
 * @param <T> тип товару, обмежений класом Product
 */
public interface OrderRepository<T extends Product> {

    void save(Order<T> order);

    Optional<Order<T>> findById(String id);

    List<Order<T>> findAll();

    List<Order<T>> findBy(Predicate<? super Order<T>> condition);

    default List<Order<T>> findByStatus(OrderStatus status) {
        return findBy(order -> order.getStatus() == status);
    }

    int count();
}
