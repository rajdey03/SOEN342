package src;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrainConnectionDB {

    public TrainConnectionDB() {
    }

    private static final String DB_URL = "jdbc:sqlite:my.db";

    public List<TrainConnection> getAllConnections() {
        List<TrainConnection> list = new ArrayList<>();
        String sql = "SELECT routeID, departureCity, arrivalCity, departureTime, arrivalTime, trainType, daysOfOperation, "
                + "firstClassRate, secondClassRate FROM TrainConnections";

        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement(); var rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                TrainConnection tc = new TrainConnection();
                tc.setRouteID(rs.getString("routeID"));
                tc.setDepartureCity(rs.getString("departureCity"));
                tc.setArrivalCity(rs.getString("arrivalCity"));
                tc.setDepartureTime(rs.getString("departureTime"));
                tc.setArrivalTime(rs.getString("arrivalTime"));
                tc.setTrainType(rs.getString("trainType"));
                tc.setDaysOfOperation(rs.getString("daysOfOperation"));
                tc.setFirstClassRate(rs.getInt("firstClassRate"));
                tc.setSecondClassRate(rs.getInt("secondClassRate"));
                list.add(tc);
            }

        } catch (SQLException e) {
            System.err.println("Error loading train connections: " + e.getMessage());
        }

        return list;
    }

    public TrainConnection getByRouteID(String routeID) {
        String sql = "SELECT routeID, departureCity, arrivalCity, departureTime, arrivalTime, trainType, daysOfOperation, "
                + "firstClassRate, secondClassRate FROM TrainConnections";

        try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, routeID);
            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    TrainConnection tc = new TrainConnection();
                    tc.setRouteID(rs.getString("routeID"));
                    tc.setDepartureCity(rs.getString("departureCity"));
                    tc.setArrivalCity(rs.getString("arrivalCity"));
                    tc.setDepartureTime(rs.getString("departureTime"));
                    tc.setArrivalTime(rs.getString("arrivalTime"));
                    tc.setTrainType(rs.getString("trainType"));
                    tc.setDaysOfOperation(rs.getString("daysOfOperation"));
                    tc.setFirstClassRate(rs.getInt("firstClassRate"));
                    tc.setSecondClassRate(rs.getInt("secondClassRate"));
                    return tc;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching route: " + e.getMessage());
        }

        return null;
    }

    public List<TrainConnection> getFilteredConnections(Map<String, String> filters) {
        List<TrainConnection> results = getAllConnections(); // Start with all 92

        //Apply optional filters
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String key = filter.getKey();
            String value = filter.getValue();

            results.removeIf(tc -> {
                switch (key) {
                    case "depDay":
                        return !tc.getDaysOfOperation().contains(value);

                    case "arrDay":
                        return !tc.getDaysOfOperation().contains(value);

                    case "trainType":
                        return !tc.getTrainType().equalsIgnoreCase(value);

                    case "depTime":
                        return tc.getDepartureTime().compareTo(value) < 0;

                    case "arrTime":
                        return tc.getArrivalTime().compareTo(value) > 0;

                    case "minFirstClassPrice":
                        return tc.getFirstClassRate() < Integer.parseInt(value);

                    case "maxFirstClassPrice":
                        return tc.getFirstClassRate() > Integer.parseInt(value);

                    case "minSecondClassPrice":
                        return tc.getSecondClassRate() < Integer.parseInt(value);

                    case "maxSecondClassPrice":
                        return tc.getSecondClassRate() > Integer.parseInt(value);

                    default:
                        return false;
                }
            });
        }
        return results;
    }

}
