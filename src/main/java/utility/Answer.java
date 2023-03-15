package utility;

import java.io.Serializable;

public record Answer(String text, boolean systemInformation) implements Serializable {
}
