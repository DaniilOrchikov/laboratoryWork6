package utility;

import ticket.Ticket;

import java.io.Serializable;

/**
 * Класс, используемый для передачи команды и, при необходимости объекта типа {@link Ticket} от клиента на сервер.
 */
public class Command implements Serializable {
    /**
     * Поле с объектом типа {@link Ticket}.
     */
    private Ticket t;
    /**
     * Поле с командой.
     */
    private final String[] command;
    /**
     * Поле, определяющее передается ли билет. Если true - Передается, иначе нет.
     */
    public final boolean hasTicket;

    public Command(String[] command, Ticket t) {
        this.t = t;
        this.command = command;
        this.hasTicket = true;
    }

    public Command(String[] command) {
        this.command = command;
        this.hasTicket = false;
    }

    public Ticket getTicket() {
        return t;
    }

    public String[] getCommand() {
        return command;
    }
}
