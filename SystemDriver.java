public class SystemDriver {
    public static void main(String[] args) {
        String csvPath = "resources/eu_rail_network.csv";
        TrainConnectionDB traindb = new TrainConnectionDB();
        try {
            traindb.loadCSV(csvPath);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        for (TrainConnection tc : traindb.getTrainConnections()) {
            System.out.println("Route ID: " + tc.getRouteID());
            System.out.println("Departure City: " + tc.getDepartureCity());
            System.out.println("Arrival City: " + tc.getArrivalCity());
            System.out.println("Departure Time: " + tc.getDepartureTime());
            System.out.println("Arrival Time: " + tc.getArrivalTime());
            System.out.println("Train Type: " + tc.getTrain().getTrainType());
            System.out.println("Days of Operation: " + tc.getDaysOfOperation());
            System.out.println("First Class Rate: " + tc.getFirstClassRate());
            System.out.println("Second Class Rate: " + tc.getSecondClassRate());
            System.out.println("---------------------------");
        }
    }
}
