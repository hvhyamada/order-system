package com.shop.order.app;

import com.github.javafaker.Faker;
import com.shop.order.model.Clothing;
import com.shop.order.model.Electronics;
import com.shop.order.model.Order;
import com.shop.order.model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Наповнює об'єкти випадковими даними за допомогою JavaFaker.
 * Товари створюються через Lombok Builder.
 */
public class OrderGenerator {

    private final Faker faker;
    private final AtomicInteger sequence = new AtomicInteger(1);

    public OrderGenerator(long seed) {
        this.faker = new Faker(Locale.of("uk"), new Random(seed));
    }

    public Electronics electronics() {
        return Electronics.builder()
                .id(faker.code().ean8())
                .name(faker.options().option("Ноутбук", "Смартфон", "Навушники", "Монітор", "Планшет")
                        + " " + faker.bothify("??-###").toUpperCase())
                .brand(faker.options().option("Samsung", "Lenovo", "Apple", "Xiaomi", "Asus"))
                .price(price(1500, 60000))
                .warrantyMonths(faker.options().option(12, 24, 36))
                .build();
    }

    public Clothing clothing() {
        return Clothing.builder()
                .id(faker.code().ean8())
                .name(faker.options().option("Футболка базова", "Куртка зимова", "Джинси класичні",
                        "Светр вовняний", "Сукня літня", "Худі спортивне"))
                .size(faker.options().option("XS", "S", "M", "L", "XL"))
                .material(faker.options().option("бавовна", "льон", "вовна", "поліестер"))
                .price(price(300, 5000))
                .build();
    }

    /** Замовлення на товар типу T; товар береться з постачальника (Supplier). */
    public <T extends Product> Order<T> order(Supplier<T> productSupplier) {
        return Order.<T>builder()
                .id(String.format("ORD-%03d", sequence.getAndIncrement()))
                .product(productSupplier.get())
                .quantity(faker.number().numberBetween(1, 8))
                .customer(faker.name().fullName())
                .build();
    }

    /** Список з count замовлень; постачальник передається як посилання на метод. */
    public <T extends Product> List<Order<T>> orders(int count, Supplier<T> productSupplier) {
        return Stream.generate(() -> order(productSupplier)).limit(count).toList();
    }

    private BigDecimal price(int min, int max) {
        return BigDecimal.valueOf(faker.number().randomDouble(2, min, max))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
