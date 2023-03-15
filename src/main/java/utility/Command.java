package utility;

import ticket.Ticket;

import java.io.Serializable;

public class Command implements Serializable {
    private Ticket t;
    private final String[] command;
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
