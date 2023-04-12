package ticket;

import java.io.Serializable;

/**
 * Класс адреса с полями <b>street</b> и <b>zipCode</b>
 *
 * @param street  Поле улица.
 *                Не может быть пустой, Поле не может быть null
 * @param zipCode Поле индекс.
 *                Не может быть null
 */
public record Address(String street, String zipCode) implements Serializable {
    @Override
    public String toString() {
        return String.format("{street:%s, zipCode:%s}", street, zipCode);
    }
}