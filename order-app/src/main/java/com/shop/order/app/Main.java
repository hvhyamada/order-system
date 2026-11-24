package com.shop.order.app;

import com.shop.order.model.Clothing;
import com.shop.order.model.Electronics;
import com.shop.order.model.Order;
import com.shop.order.model.OrderStatus;
import com.shop.order.model.Product;
import com.shop.order.processing.InvalidProductTypeException;
import com.shop.order.processing.OrderFilters;
import com.shop.order.processing.OrderProcessingException;
import com.shop.order.processing.OrderProcessor;
import com.shop.order.storage.OrderRepositories;
import com.shop.order.storage.OrderRepository;
import com.shop.order.threads.DispatchReport;
import com.shop.order.threads.OrderDispatcher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        OrderGenerator generator = new OrderGenerator(42);

        // 1. Генерація замовлень: постачальники товарів передаються як посилання на методи.
        List<Order<Electronics>> electronicsOrders = new ArrayList<>(generator.orders(6, generator::electronics));
        List<Order<Clothing>> clothingOrders = new ArrayList<>(generator.orders(6, generator::clothing));
        // Некоректне замовлення: кількість 0 -> OrderProcessingException під час обробки.
        clothingOrders.add(Order.<Clothing>builder()
                .id("ORD-999").product(generator.clothing()).quantity(0).customer("Тест Тестович").build());

        List<Order<? extends Product>> allOrders = Stream
                .concat(electronicsOrders.stream(), clothingOrders.stream())
                .<Order<? extends Product>>map(order -> order)
                .toList();

        title("Згенеровані замовлення (" + allOrders.size() + ")");
        allOrders.stream().map(OrderProcessor::describe).forEach(System.out::println);

        // 2. Фільтрація за типом через узагальнений метод і лямбду.
        title("Фільтрація за типом");
        List<Order<Electronics>> onlyElectronics = OrderFilters.byType(allOrders, Electronics.class);
        List<Order<Clothing>> onlyClothing = OrderFilters.byType(allOrders, Clothing.class);
        System.out.println("Електроніка: " + onlyElectronics.size() + ", одяг: " + onlyClothing.size());
        List<Order<Electronics>> expensive = OrderFilters.where(onlyElectronics,
                order -> order.getProduct().getPrice().compareTo(new BigDecimal("20000")) > 0);
        System.out.println("Електроніка дорожча за 20000 грн: " + expensive.size());

        // 3. Паралельна обробка: модуль order.threads, сховища з модуля order.storage.
        OrderProcessor<Electronics> electronicsProcessor = new OrderProcessor<>(Electronics.class);
        OrderProcessor<Clothing> clothingProcessor = new OrderProcessor<>(Clothing.class);
        OrderRepository<Electronics> electronicsRepo = OrderRepositories.inMemory(Electronics.class);
        OrderRepository<Clothing> clothingRepo = OrderRepositories.inMemory(Clothing.class);

        title("Паралельна обробка (пул з 4 потоків)");
        List<DispatchReport> reports;
        try (OrderDispatcher dispatcher = new OrderDispatcher(4, System.out::println)) {
            reports = List.of(
                    dispatcher.dispatch("Електроніка", onlyElectronics, electronicsProcessor, electronicsRepo),
                    dispatcher.dispatch("Одяг", onlyClothing, clothingProcessor, clothingRepo));
        }

        title("Підсумок");
        reports.forEach(r -> System.out.printf("%-12s оброблено: %d, помилок: %d, час: %d мс%n",
                r.category(), r.processed(), r.failed(), r.millis()));
        reports.stream().flatMap(r -> r.errors().stream()).forEach(e -> System.out.println("  ! " + e));

        BigDecimal revenue = Stream.concat(
                        electronicsRepo.findByStatus(OrderStatus.PROCESSED).stream(),
                        clothingRepo.findByStatus(OrderStatus.PROCESSED).stream())
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("У сховищах: " + (electronicsRepo.count() + clothingRepo.count())
                + " замовлень, виручка: " + revenue + " грн");

        title("Оброблені замовлення зі сховища");
        Stream.concat(electronicsRepo.findAll().stream(), clothingRepo.findAll().stream())
                .map(OrderProcessor::describe)
                .forEach(System.out::println);

        // 4. Виключення, специфічні для узагальнень.
        title("Обробка виключень узагальнень");
        demoGenericExceptions(electronicsProcessor, clothingOrders.get(0));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void demoGenericExceptions(OrderProcessor<Electronics> processor, Order<Clothing> clothingOrder) {
        // Сирий тип обходить перевірку компілятора: Order<Clothing> стає "Order<Electronics>".
        Order<Electronics> polluted = (Order) clothingOrder;
        try {
            processor.process(polluted);
        } catch (InvalidProductTypeException e) {
            System.out.println("InvalidProductTypeException: " + e.getMessage());
        } catch (OrderProcessingException e) {
            System.out.println("OrderProcessingException: " + e.getMessage());
        }

        try {
            Electronics product = OrderFilters.requireType(clothingOrder.getProduct(), Electronics.class);
            System.out.println("Отримано: " + product.getName());
        } catch (InvalidProductTypeException e) {
            System.out.println("requireType: " + e.getMessage());
        }
    }

    private static void title(String text) {
        System.out.println();
        System.out.println("=== " + text + " ===");
    }
}
