package src;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientDB {

    private static final String DB_URL = "jdbc:sqlite:my.db";

    public ClientDB() {
        // Constructor - no need to store clients in memory anymore
    }

    // Load all clients from database
    public List<Client> getClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT clientID, firstName, lastName, age FROM Client";

        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement(); var rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Client client = new Client();
                client.setClientId(rs.getString("clientID"));
                client.setFirstName(rs.getString("firstName"));
                client.setLastName(rs.getString("lastName"));
                client.setAge(rs.getInt("age"));
                clients.add(client);
            }
        } catch (SQLException e) {
            System.err.println("Error loading clients: " + e.getMessage());
        }
        return clients;
    }

    // Create and save a new client to database
    public Client createClient(String firstName, String lastName, int age) {
        Client client = new Client();
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setAge(age);

        // Generate a unique clientID using UUID
        String clientID = UUID.randomUUID().toString();
        client.setClientId(clientID);

        // Insert into database
        String sql = "INSERT INTO Client(clientID, firstName, lastName, age) VALUES(?,?,?,?)";

        try (var conn = DriverManager.getConnection(DB_URL); var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, client.getClientId());
            pstmt.setString(2, client.getFirstName());
            pstmt.setString(3, client.getLastName());
            pstmt.setInt(4, client.getAge());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error creating client: " + e.getMessage());
            return null;
        }

        return client;
    }

    // Get a specific client by last name and ID 
    public Client getClientByLastNameAndID(String lastName, String clientID) {
        String sql = "SELECT clientID, firstName, lastName, age FROM Client WHERE lastName = ? AND clientID = ?";

        try (var conn = DriverManager.getConnection(DB_URL); var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, lastName);
            pstmt.setString(2, clientID);
            var rs = pstmt.executeQuery();

            if (rs.next()) {
                Client client = new Client();
                client.setClientId(rs.getString("clientID"));
                client.setFirstName(rs.getString("firstName"));
                client.setLastName(rs.getString("lastName"));
                client.setAge(rs.getInt("age"));
                return client;
            }
        } catch (SQLException e) {
            System.err.println("Error finding client: " + e.getMessage());
        }

        return null;
    }

}
