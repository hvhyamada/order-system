package com.shop.order.threads;

import com.shop.order.model.Order;
import com.shop.order.model.Product;
import com.shop.order.processing.InvalidProductTypeException;
import com.shop.order.processing.OrderProcessingException;
import com.shop.order.processing.OrderProcessor;
import com.shop.order.storage.OrderRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Розподіляє замовлення між потоками пулу та збирає результати.
 */
public class OrderDispatcher implements AutoCloseable {

    private final ExecutorService executor;
    private final Consumer<String> log;

    public OrderDispatcher(int threads, Consumer<String> log) {
        AtomicInteger counter = new AtomicInteger(1);
        // Лямбда-вираз як фабрика потоків з іменами order-worker-N.
        ThreadFactory factory = task -> new Thread(task, "order-worker-" + counter.getAndIncrement());
        this.executor = Executors.newFixedThreadPool(threads, factory);
        this.log = log;
    }

    /**
     * Паралельно обробляє замовлення і зберігає їх у сховище.
     *
     * @param <T> тип товару; обробник і сховище мають бути для того ж типу
     */
    public <T extends Product> DispatchReport dispatch(String category,
                                                       List<Order<T>> orders,
                                                       OrderProcessor<T> processor,
                                                       OrderRepository<T> repository) {
        AtomicInteger processed = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
        long start = System.nanoTime();

        List<CompletableFuture<Void>> futures = orders.stream()
                .map(order -> CompletableFuture.runAsync(() -> {
                    String thread = Thread.currentThread().getName();
                    try {
                        processor.process(order);
                        processed.incrementAndGet();
                        log.accept("[" + thread + "] оброблено " + order.getId()
                                + " (" + order.getProduct().getName() + ") = " + order.getTotal() + " грн");
                    } catch (OrderProcessingException | InvalidProductTypeException e) {
                        failed.incrementAndGet();
                        errors.add(order.getId() + ": " + e.getMessage());
                        log.accept("[" + thread + "] помилка " + order.getId() + ": " + e.getMessage());
                    } finally {
                        if (processor.getProductType().isInstance(order.getProduct())) {
                            repository.save(order);
                        }
                    }
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        return new DispatchReport(category, processed.get(), failed.get(), List.copyOf(errors), millis);
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
