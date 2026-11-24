/**
 * Модуль управління потоками: паралельна обробка замовлень у пулі потоків.
 */
module order.threads {
    requires transitive order.processing;
    requires transitive order.storage;

    exports com.shop.order.threads;
}
