package src;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ReservationDB {
    private static final String DB_URL = "jdbc:sqlite:my.db";
    // var reservation = "CREATE TABLE IF NOT EXISTS Reservation ("
    //             + "reservationID text PRIMARY KEY,"
    //             + "routes text NOT NULL,"
    //             + "ticketID text NOT NULL,"
    //             + "clientID text NOT NULL,"
    //             + "tripID text,"
    //             + "FOREIGN KEY (ticketID) REFERENCES Ticket(ticketID),"
    //             + "FOREIGN KEY (clientID) REFERENCES Client(clientID)"
    //             + ");"

     public Reservation createReservation(Client client, String tripID, String routes) {
    if (client == null || tripID == null) {
        System.err.println("Cannot create reservation: client or tripID is null");
        return null;
    }

    Reservation reservation = new Reservation(client);
    String reservationID = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    reservation.setReservationID(reservationID);

    String sql = "INSERT INTO Reservation(reservationID, routes, clientID, tripID, ticketID) VALUES(?,?,?,?,?)";
    
    try (var conn = DriverManager.getConnection(DB_URL); 
         var pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, reservationID);
        pstmt.setString(2, routes);  // Store the routes
        pstmt.setString(3, client.getClientId());
        pstmt.setString(4, tripID);
        pstmt.setString(5, ""); // No ticket initially
        pstmt.executeUpdate();
        
        System.out.println("Reservation created successfully: " + reservationID);
    } catch (SQLException e) {
        System.err.println("Error creating reservation: " + e.getMessage());
        return null;
    }
    
    return reservation;
}

     public List<Reservation> getReservationsForClient(String clientID) {
        List<Reservation> reservations = new ArrayList<>();
        
        String sql = "SELECT reservationID, clientID, tripID, ticketID FROM Reservation WHERE clientID = ?";
        
        try (var conn = DriverManager.getConnection(DB_URL); 
             var pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, clientID);
            var rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Reservation reservation = new Reservation(null); // Client needs to be loaded separately
                reservation.setReservationID(rs.getString("reservationID"));
                reservations.add(reservation);
            }
        } catch (SQLException e) {
            System.err.println("Error getting reservations for client: " + e.getMessage());
        }
        
        return reservations;
    }



}

