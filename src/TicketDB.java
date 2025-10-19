package src;

import java.util.List;

public class TicketDB {
    List<Ticket> tickets;

    public TicketDB() {
        tickets = new java.util.ArrayList<>();
    }

    public Ticket createTicket() {
        Ticket ticket = new Ticket();
        tickets.add(ticket);
        return ticket;
    }
}
