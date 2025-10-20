package src;

public class Ticket {
    private int ticketId;
    private static int nextId = 1;
    private double cost;

    public Ticket() {
        this.ticketId = nextId++;
    }

    public long getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
