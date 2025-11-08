package src;

public class Reservation {
    private Ticket ticket;
    private Client client;
    private String reservationID;
    private String ticketID;

    public Reservation(Client client) {
        this.client = client;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setReservationID(String reservationID){
        this.reservationID = reservationID;
    }

    public String getReservationID(){
        return reservationID;
    }

    public String getTicketID(){
        return ticketID;
    }

    public void setTicketID(String ticketID){
        this.ticketID = ticketID;
    }
}
