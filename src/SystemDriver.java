package src;

import java.util.*;
import java.util.stream.Collectors;

public class SystemDriver {
    static String userArrivalCity;
    static String userDepartureCity;
    static TrainConnectionDB trainDB = new TrainConnectionDB();
    static ClientDB clientDB = new ClientDB();
    static TripDB tripDB = new TripDB();
    static TicketDB ticketDB = new TicketDB();
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
                    if (trainConnections == null || trainConnections.isEmpty()) {
                        System.out.println("No connections found. Returning to main menu.");
                        break;
                    }
                    displayAllConnections(trainConnections, departureCity, arrivalCity);

                    boolean subMenu = true;
                    while (subMenu) {
                        System.out.println("\nWould you like to sort the results?");
                        System.out.println("1. Sort by Duration");
                        System.out.println("2. Sort by First Class Rate");
                        System.out.println("3. Sort by Second Class Rate");
                        System.out.println("4. Go back");

                        System.out.println("\nReady to book a trip?");
                        System.out.println("5. Select your connection(s)");

                        System.out.println("6. Exit");
                        System.out.print("Select an option: ");
                        String subChoice = scanner.nextLine();
                        switch (subChoice) {
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
                                subMenu = false;
                                break;
                            case "5":
                                System.out.println("\nPlease select your desired trip from the displayed list above.");
                                int userTripOption = Integer.parseInt(scanner.nextLine());
                                bookTrip(userTripOption);


                                break;
                            case "6":
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
                    
                    switch(filterChoice) {
                        case "1":
                            System.out.print("Enter Departure Day (e.g., Monday): ");
                            String depDay = scanner.nextLine();
                            updateInputs("depDay", depDay);
                            break;
                        case "2":
                            System.out.print("Enter Arrival Day (e.g., Monday): ");
                            String arrDay = scanner.nextLine();
                            updateInputs("arrDay", arrDay);
                            break;
                        case "3":
                            System.out.print("Enter Train Type (e.g., High-speed, Regional): ");
                            String trainType = scanner.nextLine();
                            updateInputs("trainType", trainType);
                            break;
                        case "4":
                            System.out.print("Enter Earliest Departure Time (HH:MM): ");
                            String depTime = scanner.nextLine();
                            updateInputs("depTime", depTime);
                            break;
                        case "5":
                            System.out.print("Enter Latest Arrival Time (HH:MM): ");
                            String arrTime = scanner.nextLine();
                            updateInputs("arrTime", arrTime);
                            break;
                        case "6":
                            System.out.print("Enter Minimum price for First Class: ");
                            String minFirstClass = scanner.nextLine();
                            updateInputs("minFirstClassPrice", minFirstClass);
                            break;
                        case "7":
                            System.out.print("Enter Maximum price for First Class: ");
                            String maxFirstClass = scanner.nextLine();
                            updateInputs("maxFirstClassPrice", maxFirstClass);
                            break;
                        case "8":
                            System.out.print("Enter Minimum price for Second Class: ");
                            String minSecondClass = scanner.nextLine();
                            updateInputs("minSecondClassPrice", minSecondClass);
                            break;
                        case "9":
                            System.out.print("Enter Maximum price for Second Class: ");
                            String maxSecondClass = scanner.nextLine();
                            updateInputs("maxSecondClassPrice", maxSecondClass);
                            break;
                        case "10":
                            // Go back to main menu TEST THIS!!!
                            break;
                        default:
                            System.out.println("Invalid option. Please try again.");
                            break;
                    }

                    break;

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
        int j = 0;
        while (i < trainConnections.size()) {

            System.out.println("\n--- Trip Option " + (++j) + " ---");
            trainConnections.get(i).setTripOptionNumber(j);

            if (i + 2 < trainConnections.size() &&
                    trainConnections.get(i).getDepartureCity().equalsIgnoreCase(departureCity) &&
                    trainConnections.get(i+2).getArrivalCity().equalsIgnoreCase(arrivalCity)) {
                trainConnections.get(i+1).setTripOptionNumber(j);
                trainConnections.get(i+2).setTripOptionNumber(j);
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
                    trainConnections.get(i).getArrivalCity().equalsIgnoreCase(trainConnections.get(i+1).getDepartureCity()) &&
                    trainConnections.get(i+1).getArrivalCity().equalsIgnoreCase(arrivalCity)) {
                trainConnections.get(i+1).setTripOptionNumber(j);
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

 private static Map<String,String> gatherFilters(){
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


    public static void updateInputs(String option, String value){
         if (validateInput(option, value)){
             filters.put(option, value);
             recordInput(userArrivalCity, userDepartureCity, option, value);
         }
         else {
             System.out.println("Invalid input. Please try again.");
         }
          List<TrainConnection> filteredConnections = search();
        if (filteredConnections != null && !filteredConnections.isEmpty()){
            System.out.println("Filters applied successfully. Found " + filteredConnections.size() + " connections.");
            displayAllConnections(filteredConnections, userDepartureCity, userArrivalCity);
            showSortingMenu(filteredConnections, userDepartureCity, userArrivalCity, new Scanner(System.in));
        }
        else {
             System.out.println("No connections found with the applied filters.");
        }

    }

    // Sorting menu extracted for reuse after search and after filters
    private static void showSortingMenu(List<TrainConnection> trainConnections, String departureCity, String arrivalCity, Scanner scanner) {
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
    }

    public static boolean validateInput(String option, String value){
        switch (option){
            case "depDay", "arrDay", "trainType": //departure day, arrival day, train type
                 return value != null && !value.trim().isEmpty();
            case "depTime", "arrTime": // departure time, arrival time
                return value.matches("([01]?\\d|2[0-3]):[0-5]\\d");
            case "minFirstClassPrice", "maxFirstClassPrice", "minSecondClassPrice", "maxSecondClassPrice": // min max prices
                try {
                double d = Double.parseDouble(value);
                return d >= 0;
                } catch (NumberFormatException e) {
                    return false;
                }
            default:
                return false;
        }
    }

    public static void recordInput(String arrivalCity, String departureCity, String option, String value){
        System.out.println("Recorded input for route " + departureCity + " → " + arrivalCity + ": "
            + option + " = " + value);
    }

    public static void bookTrip(int userTripOption){
        List<TrainConnection> selectedRoutes = getRoutes(search(), userTripOption);
        Trip trip = tripDB.createTrip(selectedRoutes);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            Client c = clientDB.createClient();

            System.out.println("Enter your first name: ");
            c.setFirstName(scanner.nextLine().trim());
            System.out.println("Enter your last name: ");
            c.setLastName(scanner.nextLine().trim());
            System.out.println("Enter your id: ");
            while (true) {
                // to do: implement logic for validating unique numeric IDs
                String idInput = scanner.nextLine().trim();
                try {
                    c.setClientId(Long.parseLong(idInput));
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid id. Enter a numeric id:");
                }
            }

            Reservation r = createReservation(c);

            tripDB.addReservationToTrip(trip, r);

            r.setTicket(ticketDB.createTicket());

            System.out.println("Add another traveller? (y/n)");
            String more = scanner.nextLine().trim().toLowerCase();
            if (!more.equals("y") && !more.equals("yes")) {
                break;
            }
        }
    }

    

    public static List<TrainConnection> getRoutes(List<TrainConnection> connections, int userOptionNumber) {
        if (connections == null) {
            return Collections.emptyList();
        }
        return connections.stream()
                .filter(tc -> tc.getTripOptionNumber() == userOptionNumber)
                .collect(Collectors.toList());
    }

    public static Reservation createReservation(Client client){
        Reservation r = new Reservation(client);
        return r;
    }



}

