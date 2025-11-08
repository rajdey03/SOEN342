package src;

import java.util.UUID;

public class Ticket {
    private String ticketId;  // Changed to String
    private double cost;

    public Ticket() {
        // Generate unique UUID-based ticket ID
        this.ticketId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
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