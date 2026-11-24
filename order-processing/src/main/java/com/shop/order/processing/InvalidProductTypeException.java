package com.shop.order.processing;

/**
 * Виключення, специфічне для узагальнень: у узагальнений обробник потрапив товар
 * не того типу (наприклад, через сирий тип або неперевірене приведення).
 */
public class InvalidProductTypeException extends RuntimeException {

    private final Class<?> expected;
    private final Class<?> actual;

    public InvalidProductTypeException(Class<?> expected, Class<?> actual) {
        super("Очікувався товар типу " + expected.getSimpleName()
                + ", отримано " + (actual == null ? "null" : actual.getSimpleName()));
        this.expected = expected;
        this.actual = actual;
    }

    public Class<?> getExpected() {
        return expected;
    }

    public Class<?> getActual() {
        return actual;
    }
}
