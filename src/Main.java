package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.DriverManager;
import java.sql.SQLException;

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
                + "dayOfOperation text NOT NULL,"
                + "firstClassRate INTEGER NOT NULL,"
                + "secondClassRate INTEGER NOT NULL,"
                + "trainType text NOT NULL"
                + ");";

        var train = "CREATE TABLE IF NOT EXISTS Train ("
                + "trainID text PRIMARY KEY,"
                + "trainType text NOT NULL"
                + ");";

        var reservation = "CREATE TABLE IF NOT EXISTS Reservation ("
                + "reservationID text PRIMARY KEY,"
                + "routes text NOT NULL,"
                + "ticketID text NOT NULL,"
                + "clientID text NOT NULL,"
                + "FOREIGN KEY (ticketID) REFERENCES Ticket(ticketID),"
                + "FOREIGN KEY (clientID) REFERENCES Client(clientID)"
                + ");";

        var client = "CREATE TABLE IF NOT EXISTS Client ("
                + "clientID text PRIMARY KEY,"
                + "firstName text NOT NULL,"
                + "lastName text NOT NULL,"
                + "age INTEGER NOT NULL"
                + ");";

        var trip = "CREATE TABLE IF NOT EXISTS Trip ("
                + "tripID text PRIMARY KEY,"
                + "status TEXT NOT NULL,"
                + "tripDuration INTEGER NOT NULL,"
                + "reservationID text NOT NULL,"
                + "clientID text NOT NULL,"
                + "routeID text NOT NULL,"
                + "FOREIGN KEY (reservationID) REFERENCES Reservation(reservationID),"
                + "FOREIGN KEY (clientID) REFERENCES Client(clientID)"
                + ");";

        var ticket = "CREATE TABLE IF NOT EXISTS Ticket ("
                + "ticketID text PRIMARY KEY,"
                + "totalCost INTEGER NOT NULL"
                + ");";

        try (var conn = DriverManager.getConnection(url); 
        var stmt = conn.createStatement()) {
            // enable foreign key constraints
            stmt.execute("PRAGMA foreign_keys = ON;");

            // create new tables
            stmt.execute(train);
            stmt.execute(client);
            stmt.execute(trainConnections);
            stmt.execute(ticket);
            stmt.execute(reservation);
            stmt.execute(trip);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // insert trainConnections data (from the csv) into trainConnections table
        String sql = "INSERT INTO TrainConnections(routeID, departureCity, arrivalCity, dayOfOperation, firstClassRate, secondClassRate, trainType) VALUES(?,?,?,?,?,?,?)";

        try (var conn = DriverManager.getConnection(url);
            BufferedReader br = new BufferedReader(new FileReader("resources/eu_rail_network.csv"));
            var pstmt = conn.prepareStatement(sql)) {
            String line;

            while ((line = br.readLine())!= null){
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                pstmt.setString(1, data[0]);
                pstmt.setString(2, data[1]);
                pstmt.setString(3, data[2]);
                pstmt.setString(4, data[6]);
                pstmt.setString(5, data[7]);
                pstmt.setString(6, data[8]);
                pstmt.setString(7, data[5]);
                pstmt.executeUpdate(); 
            }
            System.out.println("CSV data inserted successfully.");
        } catch (Exception e) {
            e.printStackTrace();   
        }
    }
}
