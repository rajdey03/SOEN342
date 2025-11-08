package src;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

        // Save to database with auto-generated ID
        String sql = "INSERT INTO Trip(status, tripDuration) VALUES(?,?)";

        try (var conn = DriverManager.getConnection(DB_URL); 
             var pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, trip.getStatus());
            pstmt.setDouble(2, trip.getTripDuration());
            pstmt.executeUpdate();

            // Get the auto-generated numerical ID
            var rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                String generatedId = String.valueOf(rs.getInt(1));
                trip.setTripId(generatedId);
                System.out.println("Trip created successfully: " + generatedId);
            }

        } catch (SQLException e) {
            System.err.println("Error creating trip: " + e.getMessage());
            return null;
        }

        return trip;
    }

    public void addReservationToTrip(Trip trip, Reservation reservation) {
        trip.addReservation(reservation);

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

    public List<Trip> getTripsForClient(Client client) {
        List<Trip> clientTrips = new ArrayList<>();
        if (client == null) {
            return clientTrips;
        }

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

                loadReservationsForTrip(trip);
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
                Client client = new Client();
                client.setClientId(rs.getString("clientID"));
                client.setFirstName(rs.getString("firstName"));
                client.setLastName(rs.getString("lastName"));
                client.setAge(rs.getInt("age"));

                Reservation reservation = new Reservation(client);
                reservation.setReservationID(rs.getString("reservationID"));

                String ticketID = rs.getString("ticketID");
                if (ticketID != null && !ticketID.isEmpty()) {
                    Ticket ticket = new Ticket();
                    ticket.setTicketId(ticketID);
                    reservation.setTicket(ticket);
                }

                trip.addReservation(reservation);
            }
        } catch (SQLException e) {
            System.err.println("Error loading reservations for trip: " + e.getMessage());
        }
    }

    private void loadRoutesForTrip(Trip trip) {
        //maybe implemented later?
    }
}