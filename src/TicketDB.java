package src;

import java.sql.DriverManager;
import java.sql.SQLException;

public class TicketDB {

    private static final String DB_URL = "jdbc:sqlite:my.db";

    public TicketDB() {
    }

    public Ticket createTicket() {
        Ticket ticket = new Ticket();
        String ticketID = String.valueOf(ticket.getTicketId());

        String sql = "INSERT INTO Ticket(ticketID, totalCost) VALUES(?,?)";

        try (var conn = DriverManager.getConnection(DB_URL); var pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticketID);
            pstmt.setDouble(2, ticket.getCost());
            pstmt.executeUpdate();

            System.out.println("Ticket created successfully: " + ticket.getTicketId());
        } catch (SQLException e) {
            System.err.println("Error creating ticket: " + e.getMessage());
            return null;
        }
        return ticket;
    }
}
