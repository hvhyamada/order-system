package com.shop.order.storage;

import com.shop.order.model.Clothing;
import com.shop.order.model.Electronics;
import com.shop.order.model.Order;
import com.shop.order.processing.InvalidProductTypeException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    private static Order<Electronics> order(String id) {
        Electronics phone = Electronics.builder().id("P").name("Смартфон").brand("Xiaomi")
                .price(new BigDecimal("8000")).warrantyMonths(12).build();
        return Order.<Electronics>builder().id(id).product(phone).quantity(1).customer("Клієнт").build();
    }

    @Test
    void saveAndFind() {
        OrderRepository<Electronics> repo = OrderRepositories.inMemory(Electronics.class);
        repo.save(order("O1"));
        assertTrue(repo.findById("O1").isPresent());
        assertTrue(repo.findById("NONE").isEmpty());
        assertEquals(1, repo.count());
    }

    @Test
    void concurrentSavesAreThreadSafe() throws InterruptedException {
        OrderRepository<Electronics> repo = OrderRepositories.inMemory(Electronics.class);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        IntStream.range(0, 1000).forEach(i -> pool.submit(() -> repo.save(order("O" + i))));
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(1000, repo.count());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void rejectsWrongProductType() {
        OrderRepository<Electronics> repo = OrderRepositories.inMemory(Electronics.class);
        Clothing coat = Clothing.builder().id("C").name("Куртка").size("L").material("вовна")
                .price(new BigDecimal("3000")).build();
        Order raw = Order.<Clothing>builder().id("X").product(coat).quantity(1).build();
        assertThrows(InvalidProductTypeException.class, () -> repo.save(raw));
    }
}
