/**
 * Модуль запуску: генерує замовлення (JavaFaker) і запускає обробку.
 * order.processing і order.storage доступні транзитивно через order.threads.
 */
module order.app {
    requires order.threads;
    requires javafaker;
    // SnakeYAML (залежність JavaFaker) використовує java.sql.Date,
    // тому модуль java.sql треба явно додати до графа модулів.
    requires java.sql;
}
