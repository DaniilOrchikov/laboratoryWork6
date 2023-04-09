package utility;

import ticket.Ticket;
import ticket.TicketBuilder;

import java.io.Serializable;

/**
 * Класс, используемый для передачи команды и, при необходимости объекта типа {@link Ticket} от клиента на сервер.
 */
public class Command implements Serializable {
    /**
     * Поле с объектом типа {@link Ticket}.
     */
    private TicketBuilder tb;
    /**
     * Поле с командой.
     */
    private final String[] command;
    /**
     * Поле, определяющее передается ли билет. Если true - Передается, иначе нет.
     */
    public final boolean hasTicket;
    public final String name;
    public final String password;

    public Command(String[] command, TicketBuilder tb, String name, String password) {
        this.name = name;
        this.password = password;
        this.tb = tb;
        this.command = command;
        this.hasTicket = true;
    }

    public Command(String[] command, String name, String password) {
        this.name = name;
        this.password = password;
        this.command = command;
        this.hasTicket = false;
    }

    public TicketBuilder getTicketBuilder() {
        return tb;
    }

    public String[] getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
