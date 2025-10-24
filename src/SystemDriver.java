package src;

import java.util.*;
import java.util.stream.Collectors;

public class SystemDriver {
    private static final String CSV_PATH = "resources/eu_rail_network.csv";

    // user inputs (shared state)
    static String userArrivalCity;
    static String userDepartureCity;

    // data access
    static TrainConnectionDB trainDB = new TrainConnectionDB();
    static ClientDB clientDB = new ClientDB();
    static TripDB tripDB = new TripDB();
    static TicketDB ticketDB = new TicketDB();
    static Client client;

    // filters map holds optional filter values keyed by filter name (e.g. "depDay" -> "monday")
    static Map<String, String> filters = new HashMap<>();

    // sort toggling state: remember last parameter and whether the next sort should be ascending
    List<List<TrainConnection>> filteredRoutes;
    static String lastSortParameter = null;
    static boolean ascending = true;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            trainDB.loadCSV(CSV_PATH);
            clientDB.loadClientsFromFile("clients.txt"); // Load saved clients
        System.out.println("Loaded " + clientDB.getClients().size() + " clients from file.");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        runMainLoop(scanner);
    }


    /* ---------------------------------------------------------------------
     * Main loop and menu routing
     * ------------------------------------------------------------------*/

    private static void runMainLoop(Scanner scanner) {
        boolean running = true;
        String departureCity = "";
        String arrivalCity = "";
        while (running) {
            System.out.println("------------ Train Connection System ------------");
            System.out.println("Welcome to the Train Connection System!");

            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter departure city: ");
                    departureCity = scanner.nextLine().trim();
                    addDeparture(departureCity); // record shared state

                    System.out.print("Enter arrival city: ");
                    arrivalCity = scanner.nextLine().trim();
                    addArrival(arrivalCity);
                    // Run search and provide sorting/booking submenu
                    handleSearchList(scanner, departureCity, arrivalCity);
                    break;
                case "2":
                    if (departureCity == "" || arrivalCity == ""){
                        System.out.println("Invalid option. Please try again.");
                        break;
                    }
                    else{
                        handleAddInputsFlow(scanner);
                        break;
                    }
                case "3":
                    login();
                    List<Trip> clientTrips = tripDB.getTripsForClient(client);

                    if (clientTrips.isEmpty()) {
                        System.out.println("No trips found for " + client.getFirstName() + " " + client.getLastName());
                    } else {
                        System.out.println("\n=== Trips for " + client.getFirstName() + " " + client.getLastName() + " ===\n");
                        for (Trip t : clientTrips) {
                        System.out.println(t.getSummary());
                        }
                    }
                    break;
                case "4":
                    running = false;
                    System.out.println("Exiting the system. Thank you for using our Train Connection System!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\nChoose an option:");
        System.out.println("1. Search for connections");
        System.out.println("2. Add additional inputs");
        System.out.println("3. View My Trips");
        System.out.println("4. Exit");
        System.out.print("Select an option: ");
    }



    /* ---------------------------------------------------------------------
     * Search flow and sorting UI
     * ------------------------------------------------------------------*/

    // Handles the search result presentation and the per-search submenu (sort/book).
    private static void handleSearchList(Scanner scanner, String departureCity, String arrivalCity) {
        List<TrainConnection> trainConnections = search();

        System.out.println("\n------------ Found connections: ------------");
        if (trainConnections == null || trainConnections.isEmpty()) {
            // nothing found — return to main menu
            System.out.println("No connections found. Returning to main menu.");
            return;
        }

        // Print the available options (direct & indirect routes are assembled by displayAllConnections)
        List<List<TrainConnection>> routes = getFilteredRoutes(trainConnections, departureCity, arrivalCity);
        displayAllConnections(routes);

        // Allow the user to repeatedly toggle sorting or choose to book
        boolean subMenu = true;
        while (subMenu) {
            printSearchSubMenu();
            String subChoice = scanner.nextLine().trim();
            switch (subChoice) {
                case "1":
                    // Toggle sort by duration (ascending first click, toggles each subsequent click)
                    toggleSort("duration", routes);
                    displayAllConnections(routes);
                    break;
                case "2":
                    toggleSort("firstClassRate", routes);
                    displayAllConnections(routes);
                    break;
                case "3":
                    toggleSort("secondClassRate", routes);
                    displayAllConnections(routes);
                    break;
                case "4":
                    // back to main menu
                    subMenu = false;
                    break;
                case "5":
                    // Book a trip (user supplies the trip option index shown in displayAllConnections)
                    System.out.println("\nPlease select your desired trip from the displayed list above.");
                    try {
                        int userTripOption = Integer.parseInt(scanner.nextLine().trim());
                        bookTrip(userTripOption);
                        subMenu = false;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number. Returning to submenu.");
                    }
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
    }

    private static void printSearchSubMenu() {
        System.out.println("\nWould you like to sort the results?");
        System.out.println("1. Sort by Duration");
        System.out.println("2. Sort by First Class Rate");
        System.out.println("3. Sort by Second Class Rate");
        System.out.println("4. Go back");
        System.out.println("\nReady to book a trip?");
        System.out.println("5. Select your connection(s)");
        System.out.println("6. Exit");
        System.out.print("Select an option: ");
    }
   


    /* ---------------------------------------------------------------------
     * Add inputs / filters flow
     * ------------------------------------------------------------------*/

    private static void handleAddInputsFlow(Scanner scanner) {
        printFilterMenu();
        String filterChoice = scanner.nextLine().trim();

        switch (filterChoice) {
            case "1":
                System.out.print("Enter Departure Day (e.g., Monday): ");
                updateInputs("depDay", scanner.nextLine().trim());
                break;
            case "2":
                System.out.print("Enter Arrival Day (e.g., Monday): ");
                updateInputs("arrDay", scanner.nextLine().trim());
                break;
            case "3":
                System.out.print("Enter Train Type (e.g., High-speed, Regional): ");
                updateInputs("trainType", scanner.nextLine().trim());
                break;
            case "4":
                System.out.print("Enter Earliest Departure Time (HH:MM): ");
                updateInputs("depTime", scanner.nextLine().trim());
                break;
            case "5":
                System.out.print("Enter Latest Arrival Time (HH:MM): ");
                updateInputs("arrTime", scanner.nextLine().trim());
                break;
            case "6":
                System.out.print("Enter Minimum price for First Class: ");
                updateInputs("minFirstClassPrice", scanner.nextLine().trim());
                break;
            case "7":
                System.out.print("Enter Maximum price for First Class: ");
                updateInputs("maxFirstClassPrice", scanner.nextLine().trim());
                break;
            case "8":
                System.out.print("Enter Minimum price for Second Class: ");
                updateInputs("minSecondClassPrice", scanner.nextLine().trim());
                break;
            case "9":
                System.out.print("Enter Maximum price for Second Class: ");
                updateInputs("maxSecondClassPrice", scanner.nextLine().trim());
                break;
            case "10":
                // go back
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }

    private static void printFilterMenu() {
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
    }



    /* ---------------------------------------------------------------------
     * Display helpers and route assembly
     * ------------------------------------------------------------------*/

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

    /*
     Build all possible routes between departureCity -> arrivalCity from the provided flat
     list of TrainConnection objects.
     */
    private static List<List<TrainConnection>> findAllRoutes(List<TrainConnection> trainConnections, String departureCity, String arrivalCity) {
        List<List<TrainConnection>> allRoutes = new ArrayList<>();

        // Remove duplicate connections (requires equals/hashCode in TrainConnection)
        trainConnections = trainConnections.stream()
                .distinct() // implement equals/hashCode in TrainConnection
                .collect(Collectors.toList());

        Map<String, List<TrainConnection>> depMap = new HashMap<>();
        for (TrainConnection tc : trainConnections) {
            depMap.computeIfAbsent(tc.getDepartureCity().toLowerCase(), k -> new ArrayList<>()).add(tc);
        }
        // start DFS from the desired departure city
        findRoutesDFS(depMap, departureCity.toLowerCase(), arrivalCity.toLowerCase(), new ArrayList<>(), allRoutes, 0);
        return allRoutes;
    }

    /*
     Depth-first search to assemble routes. Depth parameter limits the stops (0 => direct allowed, depth >2 stops recursion).
     */
    private static void findRoutesDFS(Map<String, List<TrainConnection>> depMap, String currentCity, String arrivalCity,
                                      List<TrainConnection> path, List<List<TrainConnection>> allRoutes, int depth) {
        if (depth > 2) return; // restrict to at most 3 legs (2 stops)
        List<TrainConnection> nextConnections = depMap.getOrDefault(currentCity, Collections.emptyList());
        for (TrainConnection tc : nextConnections) {
            if (path.contains(tc)) continue; // prevent cycles by reusing same connection object
            path.add(tc);
            if (tc.getArrivalCity().equalsIgnoreCase(arrivalCity)) {
                // reached destination: copy the path as a complete route
                allRoutes.add(new ArrayList<>(path));
            } else {
                // otherwise continue searching from the arrival city of this leg
                findRoutesDFS(depMap, tc.getArrivalCity().toLowerCase(), arrivalCity, path, allRoutes, depth + 1);
            }
            // backtrack one step
            path.remove(path.size() - 1);
        }
    }

    // Builds and returns all valid routes with the minimum number of legs between two cities.
    private static List<List<TrainConnection>> getFilteredRoutes(
            List<TrainConnection> trainConnections, String departureCity, String arrivalCity) {

        List<List<TrainConnection>> allRoutes = findAllRoutes(trainConnections, departureCity, arrivalCity);

        // Find the minimum number of legs among all routes
        int minLegs = allRoutes.stream().mapToInt(List::size).min().orElse(Integer.MAX_VALUE);

        // Only keep routes with the minimum number of legs
        return allRoutes.stream()
                .filter(route -> route.size() == minLegs)
                .collect(Collectors.toList());
    }

    private static void displayAllConnections(List<List<TrainConnection>> routes) {
        if (routes.isEmpty()) {
            System.out.println("No valid routes found.");
            return;
        }

        for (int i = 0; i < routes.size(); i++) {
            List<TrainConnection> route = routes.get(i);
            System.out.println("\n--- Trip Option " + (i + 1) + " ---");
            for (TrainConnection tc : route) {
                displayConnection(tc);
            }

            double totalDuration = 0.0;
            for (int j = 0; j < route.size(); j++) {
                totalDuration += route.get(j).getDuration();
                if (j > 0) {
                    totalDuration += calculateTransferTime(route.get(j - 1), route.get(j));
                }
            }
            System.out.println("Total Duration: " + totalDuration + " hours");
        }
    }


    /* ---------------------------------------------------------------------
     * Search and related helpers
     * ------------------------------------------------------------------*/

    // Performs a search for matching TrainConnection objects using current userDepartureCity/userArrivalCity and optional filters.
    public static List<TrainConnection> search() {
        DurationCalculator durationCalculator = new DurationCalculator();
        Map<String, String> appliedFilters = gatherFilters();
        List<TrainConnection> connectionsList = trainDB.findConnections(userDepartureCity, userArrivalCity, appliedFilters);

        if (connectionsList != null && !connectionsList.isEmpty()) {
            for (TrainConnection tc : connectionsList) {
                double duration = durationCalculator.computeAllTripDurations(tc);
                tc.setDuration(duration); // store duration on the connection for sorting/display
            }
            return connectionsList;
        } else {
            // No direct connections found — try indirect routes (1-stop and 2-stops as implemented in DB)
            List<TrainConnection> indirectConnections = trainDB.findIndirectConnections(userDepartureCity, userArrivalCity);
            for (TrainConnection tc : indirectConnections) {
                double duration = durationCalculator.computeAllTripDurations(tc);
                tc.setDuration(duration);
            }
            return indirectConnections;
        }
    }

    //Return the currently-recorded filters map. Kept as a method to centralize potential future transformations.
    private static Map<String, String> gatherFilters() {
        return filters;
    }



    /* ---------------------------------------------------------------------
     * Sorting
     * ------------------------------------------------------------------*/

    /*
     Toggle sorting order for the provided parameter. If the same parameter is requested twice in a row,
     the ordering flips (ascending <-> descending). Otherwise ordering resets to ascending for the new parameter.
     */
    public static List<List<TrainConnection>> toggleSort(
            String sortParameter, List<List<TrainConnection>> routes) {

        if (sortParameter.equals(lastSortParameter)) {
            ascending = !ascending;
        } else {
            ascending = true;
            lastSortParameter = sortParameter;
        }

        if (sortParameter.equals("duration")) {
            routes.sort(Comparator.comparingDouble(SystemDriver::computeTotalDuration));
            if (!ascending) Collections.reverse(routes);
        } else if (sortParameter.equals("firstClassRate")) {
            routes.sort(Comparator.comparingDouble(SystemDriver::computeAverageFirstClassRate));
            if (!ascending) Collections.reverse(routes);
        } else if (sortParameter.equals("secondClassRate")) {
            routes.sort(Comparator.comparingDouble(SystemDriver::computeAverageSecondClassRate));
            if (!ascending) Collections.reverse(routes);
        }

        return routes;
    }

    /*
     Helpers to compute sorting keys for a route (a List of TrainConnection objects)
  */
    private static double computeTotalDuration(List<TrainConnection> route) {
        double total = 0.0;
        for (int i = 0; i < route.size(); i++) {
            total += route.get(i).getDuration();
            if (i > 0) total += calculateTransferTime(route.get(i - 1), route.get(i));
        }
        return total;
    }

    private static double computeAverageFirstClassRate(List<TrainConnection> route) {
        return route.stream().mapToDouble(TrainConnection::getFirstClassRate).average().orElse(Double.MAX_VALUE);
    }

    private static double computeAverageSecondClassRate(List<TrainConnection> route) {
        return route.stream().mapToDouble(TrainConnection::getSecondClassRate).average().orElse(Double.MAX_VALUE);
    }


    /* ---------------------------------------------------------------------
     * Transfer time calculation
     * ------------------------------------------------------------------*/

    /*
     Compute the time (in hours) between the arrival of the first leg and the departure of the second leg.
     The method assumes times in HH:MM format and handles overnight transfers by adding 24h when the second
     departure time is earlier than the first arrival.
     */
    private static double calculateTransferTime(TrainConnection firstLeg, TrainConnection secondLeg) {
        String firstArrival = firstLeg.getArrivalTime();
        String secondDeparture = secondLeg.getDepartureTime();

        String[] arrParts = firstArrival.split(":");
        String[] depParts = secondDeparture.split(":");

        int arrHours = Integer.parseInt(arrParts[0]);
        int arrMinutes = Integer.parseInt(arrParts[1]);
        int depHours = Integer.parseInt(depParts[0]);
        int depMinutes = Integer.parseInt(depParts[1]);

        // if the second departure clock time is earlier than arrival, assume it's on the next day
        if (depHours < arrHours || (depHours == arrHours && depMinutes < arrMinutes)) {
            depHours += 24;
        }

        int totalArrMinutes = arrHours * 60 + arrMinutes;
        int totalDepMinutes = depHours * 60 + depMinutes;

        int transferInMinutes = totalDepMinutes - totalArrMinutes;

        return transferInMinutes / 60.0;
    }



    /* ---------------------------------------------------------------------
     * Filters, validation, recording
     * ------------------------------------------------------------------*/

    /*
     Update the filters map with a single option; validate the value first. After recording the input,
     re-run the search and (if results exist) show them and allow sorting.
     option strings used by the UI: depDay, arrDay, trainType, depTime, arrTime,
     minFirstClassPrice, maxFirstClassPrice, minSecondClassPrice, maxSecondClassPrice
     */
    public static void updateInputs(String option, String value) {
        if (validateInput(option, value)) {
            filters.put(option, value);
            recordInput(userArrivalCity, userDepartureCity, option, value);
        } else {
            System.out.println("Invalid input. Please try again.");
        }
        List<TrainConnection> filteredConnections = search();
        List<List<TrainConnection>> routes = getFilteredRoutes(filteredConnections, userDepartureCity, userArrivalCity);
        if (filteredConnections != null && !filteredConnections.isEmpty()) {
            System.out.println("Filters applied successfully. Found " + filteredConnections.size() + " connections.");
            displayAllConnections(routes);
            showSortingMenu(filteredConnections, userDepartureCity, userArrivalCity, new Scanner(System.in));
        } else {
            System.out.println("No connections found with the applied filters.");
        }

    }

    // Sorting menu reused after applying filters or after a search
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

            List<List<TrainConnection>> routes = getFilteredRoutes(trainConnections, departureCity, arrivalCity);
            switch (sortChoice) {
                case "1":
                    toggleSort("duration", routes);
                    displayAllConnections(routes);
                    break;
                case "2":
                    toggleSort("firstClassRate", routes);
                    displayAllConnections(routes);
                    break;
                case "3":
                    toggleSort("secondClassRate", routes);
                    displayAllConnections(routes);
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

    //Validate a single input option/value pair. Returns true if the value is acceptable for the option.
    public static boolean validateInput(String option, String value) {
        switch (option) {
            case "depDay", "arrDay", "trainType": // departure day, arrival day, train type
                return value != null && !value.trim().isEmpty();
            case "depTime", "arrTime": // departure time, arrival time
                // Accepts 24-hour format
                return value.matches("([01]?\\d|2[0-3]):[0-5]\\d");
            case "minFirstClassPrice", "maxFirstClassPrice", "minSecondClassPrice",
                    "maxSecondClassPrice": // min/max prices
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

    public static void recordInput(String arrivalCity, String departureCity, String option, String value) {
        // Small helper that prints the recorded input; could be extended to persist user preferences
        System.out.println("Recorded input for route " + departureCity + " → " + arrivalCity + ": "
                + option + " = " + value);
    }



    /* ---------------------------------------------------------------------
     * Booking Section
     * ------------------------------------------------------------------*/
    //Login register so they can view their trip using last name and id
    public static void login() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your last name:");
        String lastName = scanner.nextLine().trim().toLowerCase();
        System.out.println("Enter your id:");
        long id;
        try {
            id = Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
            return;
        }

        int clientIndex = clientDB.findClient(lastName, id);
        if (clientIndex != -1) {
            client = clientDB.getClients().get(clientIndex);
        } else {
            System.out.println("Couldn't find account!");
        }
    }

    //Create a Trip object for the selected tripOptionNumber and collect reservations/tickets from the CLI.
    public static void bookTrip(int userTripOption) {

        List<TrainConnection> trainConnections = search();
        List<TrainConnection> selectedRoutes = getRoutes(trainConnections, userTripOption, userDepartureCity, userArrivalCity);
        Trip trip = tripDB.createTrip(selectedRoutes);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            Client c = clientDB.createClient();

            System.out.println("Enter your first name: ");
            c.setFirstName(scanner.nextLine().trim());
            System.out.println("Enter your last name: ");
            c.setLastName(scanner.nextLine().trim());
            System.out.println("Enter your age: ");
            c.setAge(Integer.parseInt(scanner.nextLine().trim()));
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

        System.out.println(trip.getSummary());

    }

    public static Reservation createReservation(Client client) {
        Reservation r = new Reservation(client);
        return r;
    }

    /*
     Select a route (a List of TrainConnection legs) from the computed routes for the departure/arrival pair.
     This variant builds route options and returns the N-th option.
     */
    public static List<TrainConnection> getRoutes(List<TrainConnection> trainConnections, int userOptionNumber, String departureCity, String arrivalCity) {
        List<List<TrainConnection>> allRoutes = findAllRoutes(trainConnections, departureCity, arrivalCity);

        // Find the minimum number of legs among all routes
        int minLegs = allRoutes.stream().mapToInt(List::size).min().orElse(Integer.MAX_VALUE);

        // Only keep routes with the minimum number of legs (prefer fewer stops)
        List<List<TrainConnection>> filteredRoutes = allRoutes.stream()
                .filter(route -> route.size() == minLegs)
                .collect(Collectors.toList());

        if (userOptionNumber < 1 || userOptionNumber > filteredRoutes.size()) {
            System.out.println("Invalid trip option selected.");
            return Collections.emptyList();
        }
        return filteredRoutes.get(userOptionNumber - 1);
    }



    /* ---------------------------------------------------------------------
     * Small helpers for recording departure/arrival
     * ------------------------------------------------------------------*/

    public static void addArrival(String city) {
        userArrivalCity = city;
    }

    public static void addDeparture(String city) {
        userDepartureCity = city;
    }
}
