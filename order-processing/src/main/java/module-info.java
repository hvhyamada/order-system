/**
 * Модуль обробки замовлень: модель товарів і замовлень, узагальнений обробник.
 * Пакет com.shop.order.processing.internal не експортується —
 * інші модулі не мають до нього доступу.
 */
module order.processing {
    requires static lombok;

    exports com.shop.order.model;
    exports com.shop.order.processing;
}
