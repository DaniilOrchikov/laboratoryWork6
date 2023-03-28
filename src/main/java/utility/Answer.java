package utility;

import java.io.Serializable;

/**
 * Рекорд, используемый для передачи ответа с сервера клиенту
 * @param text будет печататься в любом случае
 * @param systemInformation будет печататься только если {@link ConsoleWriter#inputStatus} != 1
 */
public record Answer(String text, boolean systemInformation) implements Serializable {
}
