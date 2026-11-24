package com.shop.order.processing;

import com.shop.order.model.Order;
import com.shop.order.model.Product;
import com.shop.order.processing.internal.PriceCalculator;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Узагальнений обробник замовлень. Працює лише з Product або його підтипами.
 *
 * @param <T> тип товару, обмежений зверху класом Product
 */
public class OrderProcessor<T extends Product> {

    private final Class<T> productType;

    public OrderProcessor(Class<T> productType) {
        if (productType == null) {
            throw new IllegalArgumentException("Тип товару не вказано");
        }
        this.productType = productType;
    }

    public Class<T> getProductType() {
        return productType;
    }

    /**
     * Обробляє замовлення: перевіряє тип товару і кількість, рахує суму.
     *
     * @throws InvalidProductTypeException якщо в замовленні товар іншого типу
     * @throws OrderProcessingException    якщо замовлення некоректне або потік перервано
     */
    public BigDecimal process(Order<T> order) throws OrderProcessingException {
        if (order == null) {
            throw new OrderProcessingException("Замовлення відсутнє");
        }
        // Через стирання типів у рантаймі Order<T> може містити будь-який Product
        // (сирі типи, неперевірені приведення), тому перевіряємо тип явно.
        Object product = order.getProduct();
        if (!productType.isInstance(product)) {
            order.markFailed(Thread.currentThread().getName());
            throw new InvalidProductTypeException(productType,
                    product == null ? null : product.getClass());
        }
        if (order.getQuantity() <= 0) {
            order.markFailed(Thread.currentThread().getName());
            throw new OrderProcessingException("Замовлення " + order.getId()
                    + ": кількість має бути більшою за нуль, отримано " + order.getQuantity());
        }

        simulateWork();

        BigDecimal total = PriceCalculator.total(productType.cast(product), order.getQuantity());
        order.markProcessed(total, Thread.currentThread().getName());
        return total;
    }

    /** Короткий опис замовлення; зручно передавати як посилання на метод. */
    public static String describe(Order<? extends Product> order) {
        return String.format("%s | %-11s | %-28s x%-2d | %-9s | %s",
                order.getId(),
                order.getProduct().getCategory(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.getStatus(),
                order.getTotal() == null ? "-" : order.getTotal() + " грн");
    }

    private void simulateWork() throws OrderProcessingException {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(50, 150));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderProcessingException("Обробку перервано", e);
        }
    }
}
