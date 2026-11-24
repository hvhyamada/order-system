/**
 * Модуль зберігання замовлень.
 * Експортується лише інтерфейс сховища та фабрика; реалізація
 * (пакет com.shop.order.storage.internal) прихована від інших модулів.
 */
module order.storage {
    requires transitive order.processing;

    exports com.shop.order.storage;
}
