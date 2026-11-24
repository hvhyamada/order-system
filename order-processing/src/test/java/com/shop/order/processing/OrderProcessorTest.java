package com.shop.order.processing;

import com.shop.order.model.Clothing;
import com.shop.order.model.Electronics;
import com.shop.order.model.Order;
import com.shop.order.model.OrderStatus;
import com.shop.order.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private static Electronics laptop(String price) {
        return Electronics.builder().id("E1").name("Ноутбук").brand("Lenovo")
                .price(new BigDecimal(price)).warrantyMonths(24).build();
    }

    private static Clothing shirt() {
        return Clothing.builder().id("C1").name("Футболка").size("M").material("бавовна")
                .price(new BigDecimal("500")).build();
    }

    @Test
    void builderCreatesProduct() {
        Electronics e = laptop("1000");
        assertEquals("Lenovo", e.getBrand());
        assertEquals("Електроніка", e.getCategory());
        assertEquals("Одяг", shirt().getCategory());
    }

    @Test
    void builderRejectsInvalidPrice() {
        assertThrows(IllegalArgumentException.class, () -> laptop("0"));
    }

    @Test
    void processCalculatesTotal() throws OrderProcessingException {
        Order<Electronics> order = Order.<Electronics>builder()
                .id("O1").product(laptop("1000")).quantity(2).customer("Іван").build();
        BigDecimal total = new OrderProcessor<>(Electronics.class).process(order);
        assertEquals(new BigDecimal("2000.00"), total);
        assertEquals(OrderStatus.PROCESSED, order.getStatus());
    }

    @Test
    void bulkDiscountFromFiveItems() throws OrderProcessingException {
        Order<Clothing> order = Order.<Clothing>builder()
                .id("O2").product(shirt()).quantity(5).customer("Олена").build();
        assertEquals(new BigDecimal("2250.00"), new OrderProcessor<>(Clothing.class).process(order));
    }

    @Test
    void zeroQuantityThrowsOrderProcessingException() {
        Order<Clothing> order = Order.<Clothing>builder()
                .id("O3").product(shirt()).quantity(0).customer("Петро").build();
        assertThrows(OrderProcessingException.class, () -> new OrderProcessor<>(Clothing.class).process(order));
        assertEquals(OrderStatus.FAILED, order.getStatus());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void wrongTypeThroughRawCastThrowsInvalidProductTypeException() {
        Order<Clothing> clothing = Order.<Clothing>builder()
                .id("O4").product(shirt()).quantity(1).customer("Анна").build();
        Order<Electronics> polluted = (Order) clothing;
        InvalidProductTypeException e = assertThrows(InvalidProductTypeException.class,
                () -> new OrderProcessor<>(Electronics.class).process(polluted));
        assertEquals(Electronics.class, e.getExpected());
        assertEquals(Clothing.class, e.getActual());
    }

    @Test
    void filterByTypeReturnsOnlyRequestedType() {
        List<Order<? extends Product>> all = List.of(
                Order.<Electronics>builder().id("A").product(laptop("100")).quantity(1).build(),
                Order.<Clothing>builder().id("B").product(shirt()).quantity(1).build(),
                Order.<Electronics>builder().id("C").product(laptop("200")).quantity(1).build());
        List<Order<Electronics>> electronics = OrderFilters.byType(all, Electronics.class);
        assertEquals(2, electronics.size());
        assertEquals(1, OrderFilters.byType(all, Clothing.class).size());
    }

    @Test
    void requireTypeThrowsForWrongType() {
        assertThrows(InvalidProductTypeException.class,
                () -> OrderFilters.requireType(shirt(), Electronics.class));
        assertNotNull(OrderFilters.requireType(laptop("10"), Electronics.class));
    }
}
