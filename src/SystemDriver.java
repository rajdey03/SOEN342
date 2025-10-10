package src;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SystemDriver {
    static String userArrivalCity;
    static String userDepartureCity;
    static TrainConnectionDB trainDB = new TrainConnectionDB();
    static String lastSortParameter = null;
    static boolean ascending = true;
    static Map<String,String> filters = new java.util.HashMap<>();

    public static void main(String[] args) {
        String csvPath = "resources/eu_rail_network.csv";
        Scanner scanner = new Scanner(System.in);
        try {
            trainDB.loadCSV(csvPath);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        boolean running = true;
        while (running) {
            System.out.println("------------ Train Connection System ------------");
            System.out.println("Welcome to the Train Connection System!");
            System.out.print("Enter departure city: ");
            String departureCity = scanner.nextLine();
            addDeparture(departureCity); //change case sensitive maybe
            System.out.print("Enter arrival city: ");
            String arrivalCity = scanner.nextLine();
            addArrival(arrivalCity);
            System.out.println("\n------------ Choose an option: ------------");
            System.out.println("1. Search for connections");
            System.out.println("2. Add additional inputs");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<TrainConnection> trainConnections = search();
                    //NEED TO PRINT CONNECTIONS WITH DURATION
                    System.out.println("\n------------ Found connections: ------------");
                    displayAllConnections(trainConnections, departureCity, arrivalCity);

                    boolean sortingMenu = true;
                    while (sortingMenu) {
                        System.out.println("\nWould you like to sort the results?");
                        System.out.println("1. Sort by Duration");
                        System.out.println("2. Sort by First Class Rate");
                        System.out.println("3. Sort by Second Class Rate");
                        System.out.println("4. Go back");
                        System.out.println("5. Exit");
                        System.out.print("Select an option: ");
                        String sortChoice = scanner.nextLine();
                        switch (sortChoice) {
                            case "1":
                                trainConnections = toggleSort("duration", trainConnections);
                                displayAllConnections(trainConnections, departureCity, arrivalCity);
                                break;
                            case "2":
                                trainConnections = toggleSort("firstClassRate", trainConnections);
                                displayAllConnections(trainConnections, departureCity, arrivalCity);
                                break;
                            case "3":
                                trainConnections = toggleSort("secondClassRate", trainConnections);
                                displayAllConnections(trainConnections, departureCity, arrivalCity);
                                break;
                            case "4":
                                sortingMenu = false;
                                break;
                            case "5":
                                System.out.println("Exiting the system. Thank you for using our Train Connection System!");
                                System.exit(0);
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                                break;
                        }
                    }
                    break;
                case "2":
                    System.out.println("Which input would you like to add?");
                    System.out.println("1. Departure Day");
                    System.out.println("2. Arrival Day");
                    System.out.println("3. Train Type");
                    System.out.println("4. Departure Time");
                    System.out.println("5. Arrival Time");
                    System.out.println("6. Minimum price for First Class");
                    System.out.println("7. Maximum price for First Class");
                    System.out.println("8. Minimum price for Second Class");
                    System.out.println("9. Maximum price for Second Class");
                    System.out.println("10. Go back to main menu");
                    System.out.print("Select an option: ");
                    String filterChoice = scanner.nextLine();
                    //NEED TO ADD FILTERS TO THE MAP

                case "3":
                    running = false;
                    System.out.println("Exiting the system. Thank you for using our Train Connection System!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private static void displayConnection(TrainConnection tc) {
        System.out.println(
                "From: " + tc.getDepartureCity() +
                ", To: " + tc.getArrivalCity() +
                ", Departure: " + tc.getDepartureTime() +
                ", Arrival: " + tc.getArrivalTime() +
                ", Train: " + tc.getTrain().getTrainType() +
                ", 1st Class: " + tc.getFirstClassRate() +
                ", 2nd Class: " + tc.getSecondClassRate());
    }

    private static void displayAllConnections(List<TrainConnection> trainConnections, String departureCity, String arrivalCity) {
        int i = 0;
        while (i < trainConnections.size()) {

            if (i + 2 < trainConnections.size() &&
                    trainConnections.get(i).getDepartureCity().equalsIgnoreCase(departureCity) &&
                    trainConnections.get(i+2).getArrivalCity().equalsIgnoreCase(arrivalCity)) {
                System.out.println("2-stop route:");
                displayConnection(trainConnections.get(i));
                displayConnection(trainConnections.get(i+1));
                displayConnection(trainConnections.get(i+2));
                System.out.println("Total Duration: " + (trainConnections.get(i).getDuration() + trainConnections.get(i+1).getDuration() +
                        trainConnections.get(i+2).getDuration() + calculateTransferTime(trainConnections.get(i), trainConnections.get(i+1)) +
                        calculateTransferTime(trainConnections.get(i+1), trainConnections.get(i+2))) + " hours");
                System.out.println("Duration Breakdown: ");
                System.out.println("\tFirst Leg Duration: " + trainConnections.get(i).getDuration() + " hours");
                System.out.println("\tFirst Transfer Time: " + calculateTransferTime(trainConnections.get(i), trainConnections.get(i+1)) + " hours");
                System.out.println("\tSecond Leg Duration: " + trainConnections.get(i+1).getDuration() + " hours");
                System.out.println("\tSecond Transfer Time: " + calculateTransferTime(trainConnections.get(i+1), trainConnections.get(i+2)) + " hours");
                System.out.println("\tThird Leg Duration: " + trainConnections.get(i+2).getDuration() + " hours");
                i += 3;
            }

            else if (i + 1 < trainConnections.size() &&
                    trainConnections.get(i).getDepartureCity().equalsIgnoreCase(departureCity) &&
                    trainConnections.get(i+1).getArrivalCity().equalsIgnoreCase(arrivalCity)) {
                System.out.println("1-stop route:");
                displayConnection(trainConnections.get(i));
                displayConnection(trainConnections.get(i+1));
                System.out.println("Total Duration: " + (trainConnections.get(i).getDuration() + trainConnections.get(i+1).getDuration() +
                        calculateTransferTime(trainConnections.get(i), trainConnections.get(i+1))) + " hours");
                System.out.println("Duration Breakdown: ");
                System.out.println("\tFirst Leg Duration: " + trainConnections.get(i).getDuration() + " hours");
                System.out.println("\tTransfer Time: " + calculateTransferTime(trainConnections.get(i), trainConnections.get(i+1)) + " hours");
                System.out.println("\tSecond Leg Duration: " + trainConnections.get(i+1).getDuration() + " hours");
                i += 2;
            }

            else {
                System.out.println("Direct route:");
                displayConnection(trainConnections.get(i));
                System.out.println("Duration: " + trainConnections.get(i).getDuration() + " hours");
                i++;
            }
        }
    }

    public static void addArrival(String city){
        userArrivalCity = city;
    }

    public static void addDeparture(String city){
        userDepartureCity = city;
    }

    public static List<TrainConnection> search(){
        DurationCalculator durationCalculator = new DurationCalculator();
        Map<String, String> filters = gatherFilters();
        List<TrainConnection> connectionsList = trainDB.findConnections(userDepartureCity, userArrivalCity, filters);

        if (connectionsList != null && !connectionsList.isEmpty()){
            for (TrainConnection tc : connectionsList){
                double duration = durationCalculator.computeAllTripDurations(tc);
                tc.setDuration(duration);
            }
            return connectionsList;
        }

        else {
            List<TrainConnection> indirectConnections = trainDB.findIndirectConnections(userDepartureCity, userArrivalCity);
            for (TrainConnection tc : indirectConnections){
                double duration = durationCalculator.computeAllTripDurations(tc);
                tc.setDuration(duration);
            }
            return indirectConnections;
        }
    }

    private static Map<String,String> gatherFilters() {
        return filters;
    }

    public static List<TrainConnection> toggleSort(String sortParameter, List<TrainConnection> trainConnections){
        if (sortParameter.equals(lastSortParameter)){
            ascending = !ascending;
        }

        else {
            ascending = true;
            lastSortParameter = sortParameter;
        }
        sortConnections(sortParameter, trainConnections, ascending);
        return trainConnections;
    }

    private static void sortConnections(String sortParameter, List<TrainConnection> connectionsList, boolean ascending) {
        if (sortParameter.equals("duration")){
            connectionsList.sort((tc1, tc2) -> {
                if (ascending){
                    return Double.compare(tc1.getDuration(), tc2.getDuration());
                }

                else {
                    return Double.compare(tc2.getDuration(), tc1.getDuration());
                }
            });
        }

        else if (sortParameter.equals("firstClassRate")){
            connectionsList.sort((tc1, tc2) -> {
                if (ascending){
                    return Double.compare(tc1.getFirstClassRate(), tc2.getFirstClassRate());
                }

                else {
                    return Double.compare(tc2.getFirstClassRate(), tc1.getFirstClassRate());
                }
            });
        }

        else if (sortParameter.equals("secondClassRate")){
            connectionsList.sort((tc1, tc2) -> {
                if (ascending){
                    return Double.compare(tc1.getSecondClassRate(), tc2.getSecondClassRate());
                }

                else {
                    return Double.compare(tc2.getSecondClassRate(), tc1.getSecondClassRate());
                }
            });
        }
    }

    private static double calculateTransferTime(TrainConnection firstLeg, TrainConnection secondLeg) {
        String firstArrival = firstLeg.getArrivalTime();
        String secondDeparture = secondLeg.getDepartureTime();

        String[] arrParts = firstArrival.split(":");
        String[] depParts = secondDeparture.split(":");

        int arrHours = Integer.parseInt(arrParts[0]);
        int arrMinutes = Integer.parseInt(arrParts[1]);
        int depHours = Integer.parseInt(depParts[0]);
        int depMinutes = Integer.parseInt(depParts[1]);

        if (depHours < arrHours || (depHours == arrHours && depMinutes < arrMinutes)) {
            depHours += 24;
        }

        int totalArrMinutes = arrHours * 60 + arrMinutes;
        int totalDepMinutes = depHours * 60 + depMinutes;

        int transferInMinutes = totalDepMinutes - totalArrMinutes;

        return transferInMinutes / 60.0;
    }


}
