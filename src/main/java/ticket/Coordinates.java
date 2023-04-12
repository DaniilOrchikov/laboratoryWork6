package ticket;

import java.io.Serializable;

/**
 * Класс координат с полями <b>x</b> и <b>y</b>
 *
 * @param x Поле координаты x
 * @param y Поле координаты y
 */
public record Coordinates(int x, int y) implements Serializable {

    @Override
    public String toString() {
        return String.format("{x:%s, y:%s}", x, y);
    }
}
