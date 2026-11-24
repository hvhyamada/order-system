# Практична робота 4 — багатопотокова система управління замовленнями

Generics, лямбда-вирази, посилання на методи, модулі Java (JPMS), Lombok `@Builder`, JavaFaker.

## Модулі
| Модуль | Maven-модуль | Призначення | exports |
|---|---|---|---|
| `order.processing` | order-processing | моделі товарів/замовлень, `OrderProcessor<T extends Product>`, фільтри, виключення | `com.shop.order.model`, `com.shop.order.processing` |
| `order.storage` | order-storage | потокобезпечне сховище `OrderRepository<T extends Product>` | `com.shop.order.storage` |
| `order.threads` | order-threads | пул потоків `OrderDispatcher`, паралельна обробка | `com.shop.order.threads` |
| `order.app` | order-app | генерація даних (JavaFaker) і запуск | — |

Пакети `com.shop.order.processing.internal` і `com.shop.order.storage.internal` не експортуються —
з інших модулів вони недоступні (контроль доступу).

## Збірка і запуск
```
mvn clean package
java -p "order-app/target/lib;order-app/target/order-app-1.0.jar" -m order.app/com.shop.order.app.Main
```
(на Linux/macOS роздільник у `-p` — `:`)
