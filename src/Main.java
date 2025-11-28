package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String url = "jdbc:sqlite:my.db";

        // code was used to create a new database file
        //try (var conn = DriverManager.getConnection(url)) {
        //     if (conn != null) {
        //         var meta = conn.getMetaData();
        //         System.out.println("The driver name is " + meta.getDriverName());
        //         System.out.println("A new database has been created.");
        //     }
        // } catch (SQLException e) {
        //     System.err.println(e.getMessage());
        // }
        //SQL statement for creating a new TrainConnections table
        var trainConnections = "CREATE TABLE IF NOT EXISTS TrainConnections ("
                + "routeID text PRIMARY KEY,"
                + "departureCity text NOT NULL,"
                + "arrivalCity text NOT NULL,"
                + "departureTime text NOT NULL,"
                + "arrivalTime text NOT NULL,"
                + "trainType text NOT NULL,"
                + "daysOfOperation text NOT NULL,"
                + "firstClassRate INTEGER NOT NULL,"
                + "secondClassRate INTEGER NOT NULL"
                + ");";

        var reservation = "CREATE TABLE IF NOT EXISTS Reservation ("
                + "reservationID text PRIMARY KEY,"
                + "routes text,"
                + "ticketID text NOT NULL,"
                + "clientID text NOT NULL,"
                + "tripID text,"
                + "FOREIGN KEY (ticketID) REFERENCES Ticket(ticketID),"
                + "FOREIGN KEY (clientID) REFERENCES Client(clientID),"
                + "FOREIGN KEY (tripID) REFERENCES Trip(tripID)"
                + ");";

        var client = "CREATE TABLE IF NOT EXISTS Client ("
                + "clientID text PRIMARY KEY,"
                + "firstName text NOT NULL,"
                + "lastName text NOT NULL,"
                + "age INTEGER NOT NULL"
                + ");";

        var trip = "CREATE TABLE IF NOT EXISTS Trip ("
                + "tripID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "status TEXT NOT NULL,"
                + "tripDuration REAL NOT NULL"
                + ");";

        var ticket = "CREATE TABLE IF NOT EXISTS Ticket ("
                + "ticketID text PRIMARY KEY,"
                + "totalCost INTEGER NOT NULL"
                + ");";

        try (var conn = DriverManager.getConnection(url); var stmt = conn.createStatement()) {
            // enable foreign key constraints
            stmt.execute("PRAGMA foreign_keys = ON;");

            // create new tables
            stmt.execute(client);
            stmt.execute(trainConnections);
            stmt.execute(ticket);
            stmt.execute(reservation);
            stmt.execute(trip);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // insert trainConnections data (from the csv) into trainConnections table
        String sql = "INSERT INTO TrainConnections(routeID, departureCity, arrivalCity,departureTime, arrivalTime, trainType, daysOfOperation, firstClassRate, secondClassRate) VALUES(?,?,?,?,?,?,?,?,?)";

        try (var conn = DriverManager.getConnection(url); BufferedReader br = new BufferedReader(new FileReader("resources/eu_rail_network.csv")); var pstmt = conn.prepareStatement(sql)) {
            String line;

            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // Get the original day string from data[6]
                String originalDays = data[6];
                // Create the new, expanded day string
                String expandedDays = expandDays(originalDays);

                pstmt.setString(1, data[0]);
                pstmt.setString(2, data[1]);
                pstmt.setString(3, data[2]);
                pstmt.setString(4, data[3]);
                pstmt.setString(5, data[4]);
                pstmt.setString(6, data[5]);
                pstmt.setString(7, expandedDays);
                pstmt.setString(8, data[7]);
                pstmt.setString(9, data[8]);
                pstmt.executeUpdate();
            }
            System.out.println("CSV data inserted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String expandDays(String days) {
        // Use 3-letter day codes to match your CSV data
        final List<String> ALL_DAYS = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

        if ("Daily".equalsIgnoreCase(days)) {
            return String.join(", ", ALL_DAYS);
        }

        if (days.contains("-")) {
            try {
                String[] parts = days.split("-");
                String startDay = parts[0];
                String endDay = parts[1];

                int startIndex = ALL_DAYS.indexOf(startDay);
                int endIndex = ALL_DAYS.indexOf(endDay);

                if (startIndex == -1 || endIndex == -1) {
                    return days; // Not a valid range we can parse, return original
                }

                List<String> resultDays = new ArrayList<>();
                if (startIndex <= endIndex) {
                    // Normal range (e.g., "Mon-Fri")
                    for (int i = startIndex; i <= endIndex; i++) {
                        resultDays.add(ALL_DAYS.get(i));
                    }
                } else {
                    // Wraparound range (e.g., "Fri-Mon")
                    for (int i = startIndex; i < ALL_DAYS.size(); i++) {
                        resultDays.add(ALL_DAYS.get(i));
                    }
                    for (int i = 0; i <= endIndex; i++) {
                        resultDays.add(ALL_DAYS.get(i));
                    }
                }
                return String.join(", ", resultDays);
            } catch (Exception e) {
                System.err.println("Could not parse day range: " + days);
                return days;
            }
        }

        // If it's already in the "Mon, Wed, Fri" format or a single day
        return days;
    }
}
