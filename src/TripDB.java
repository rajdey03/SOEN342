package src;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TripDB {

    private static final String DB_URL = "jdbc:sqlite:my.db";

    public TripDB() {
    }

    public List<Trip> getTrips() {
        List<Trip> trips = new ArrayList<>();

        String sql = "SELECT tripID, status, tripDuration FROM Trip";
        try (var conn = DriverManager.getConnection(DB_URL); 
             var stmt = conn.createStatement(); 
             var rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Trip trip = new Trip(new ArrayList<TrainConnection>());
                trip.setTripId(rs.getString("tripID"));
                trip.setStatus(rs.getString("status"));
                trip.setTripDuration(rs.getDouble("tripDuration"));
                trips.add(trip);
            }
        } catch (SQLException e) {
            System.err.println("Error loading trips: " + e.getMessage());
        }
        return trips;
    }

    public Trip createTrip(List<TrainConnection> routes) {
        Trip trip = new Trip(routes);
        trip.setStatus("Current");

        // Calculate and set trip duration
        double totalDuration = 0.0;
        for (TrainConnection tc : routes) {
            totalDuration += tc.getDuration();
        }
        trip.setTripDuration(totalDuration);

        // Save to database - FIXED: Only 3 parameters now
        String sql = "INSERT INTO Trip(tripID, status, tripDuration) VALUES(?,?,?)";

        try (var conn = DriverManager.getConnection(DB_URL); 
             var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, trip.getTripId());
            pstmt.setString(2, trip.getStatus());
            pstmt.setDouble(3, trip.getTripDuration());
            // REMOVED the three setNull calls
            pstmt.executeUpdate();

            System.out.println("Trip created successfully: " + trip.getTripId());
        } catch (SQLException e) {
            System.err.println("Error creating trip: " + e.getMessage());
            return null;
        }

        return trip;
    }

    public void addReservationToTrip(Trip trip, Reservation reservation) {
        // Add to in-memory trip object
        trip.addReservation(reservation);

        // Update the RESERVATION table with the tripID, not the Trip table
        String sql = "UPDATE Reservation SET tripID = ? WHERE reservationID = ?";

        try (var conn = DriverManager.getConnection(DB_URL); 
             var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, trip.getTripId());
            pstmt.setString(2, reservation.getReservationID());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error adding reservation to trip: " + e.getMessage());
        }
    }

    /**
     * Get all trips for a specific client
     */
    public List<Trip> getTripsForClient(Client client) {
        List<Trip> clientTrips = new ArrayList<>();
        if (client == null) {
            return clientTrips;
        }

        // FIXED: Added space after tripDuration and removed non-existent columns
        String sql = "SELECT DISTINCT t.tripID, t.status, t.tripDuration "
                + "FROM Trip t "
                + "JOIN Reservation r ON t.tripID = r.tripID "
                + "WHERE r.clientID = ?";

        try (var conn = DriverManager.getConnection(DB_URL); 
             var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, client.getClientId());
            var rs = pstmt.executeQuery();

            while (rs.next()) {
                Trip trip = new Trip(new ArrayList<TrainConnection>());
                trip.setTripId(rs.getString("tripID"));
                trip.setStatus(rs.getString("status"));
                trip.setTripDuration(rs.getDouble("tripDuration"));
                // REMOVED these lines - these columns don't exist anymore:
                // trip.setReservationID(rs.getString("reservationID"));
                // trip.setClientId(rs.getString("clientID"));
                // trip.setRouteID(rs.getString("routeID"));

                // Load reservations for this trip
                loadReservationsForTrip(trip);

                // Load routes for this trip
                loadRoutesForTrip(trip);

                clientTrips.add(trip);
            }
        } catch (SQLException e) {
            System.err.println("Error getting trips for client: " + e.getMessage());
        }

        return clientTrips;
    }

    private void loadReservationsForTrip(Trip trip) {
        String sql = "SELECT r.reservationID, r.clientID, r.ticketID, "
                + "c.firstName, c.lastName, c.age "
                + "FROM Reservation r "
                + "JOIN Client c ON r.clientID = c.clientID "
                + "WHERE r.tripID = ?";

        try (var conn = DriverManager.getConnection(DB_URL); 
             var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, trip.getTripId());
            var rs = pstmt.executeQuery();

            while (rs.next()) {
                // Reconstruct client
                Client client = new Client();
                client.setClientId(rs.getString("clientID"));
                client.setFirstName(rs.getString("firstName"));
                client.setLastName(rs.getString("lastName"));
                client.setAge(rs.getInt("age"));

                // Create reservation
                Reservation reservation = new Reservation(client);
                reservation.setReservationID(rs.getString("reservationID"));

                // Create ticket if exists
                String ticketID = rs.getString("ticketID");
                if (ticketID != null && !ticketID.isEmpty()) {
                    Ticket ticket = new Ticket();
                    ticket.setTicketId(Integer.parseInt(ticketID));
                    reservation.setTicket(ticket);
                }

                trip.addReservation(reservation);
            }
        } catch (SQLException e) {
            System.err.println("Error loading reservations for trip: " + e.getMessage());
        }
    }

    private void loadRoutesForTrip(Trip trip) {
        // TODO: Implement route storage and retrieval
        // For now, routes are stored in memory during the session
    }
}