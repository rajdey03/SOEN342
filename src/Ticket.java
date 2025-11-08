package src;

import java.util.UUID;

public class Ticket {
    private String ticketId;
    private double cost;

    public Ticket() {
        this.ticketId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        this.cost = 0.0;
    }

    // Constructor with cost
    public Ticket(double cost) {
        this.ticketId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        this.cost = cost;
    }

    public String getTicketId() {  
        return ticketId;
    }

    public void setTicketId(String ticketId) {  
        this.ticketId = ticketId;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}