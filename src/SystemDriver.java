package src;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class SystemDriver {

    // user inputs (shared state)
    static String userArrivalCity;
    static String userDepartureCity;

    // data access
    static TrainConnectionDB trainConnectionsDB = new TrainConnectionDB();
    static ClientDB clientDB = new ClientDB();
    static TripDB tripDB = new TripDB();
    static TicketDB ticketDB = new TicketDB();
    static Client client;
    static ReservationDB reservationDB = new ReservationDB();

    static Map<String, String> filters = new HashMap<>();

    static String lastSortParameter = null;
    static boolean ascending = true;

    private static final LocalTime DAY_START = LocalTime.of(6, 0);   // 06:00 inclusive
    private static final LocalTime DAY_END = LocalTime.of(22, 0);   // 22:00 inclusive

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
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
                    if (departureCity.isEmpty() || arrivalCity.isEmpty()) {
                        System.out.println("Please set departure and arrival cities first (use option 1).");
                        break;
                    } else {
                        handleAddInputsFlow(scanner);
                        break;
                    }
                case "3":
                    // Prompt the user for credentials and display their trips via viewTrips
                    System.out.print("Enter your last name: ");
                    String lastNameInput = scanner.nextLine().trim();
                    System.out.print("Enter your id: ");
                    String idInput = scanner.nextLine().trim();
                    viewTrips(lastNameInput, idInput);
                    break;
                case "4":
                    running = false;
                    System.out.println("Exiting the system. Thank you for using our Train Connection System!");
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
        System.out.println("\nWhich input would you like to add?");
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
                "From: " + tc.getDepartureCity()
                + ", To: " + tc.getArrivalCity()
                + ", Departure: " + tc.getDepartureTime()
                + ", Arrival: " + tc.getArrivalTime()
                + ", Train: " + tc.getTrainType()
                + ", 1st Class: " + tc.getFirstClassRate()
                + ", 2nd Class: " + tc.getSecondClassRate());
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
        if (depth > 2) {
            return; // restrict to at most 3 legs (2 stops)

        }
        List<TrainConnection> nextConnections = depMap.getOrDefault(currentCity, Collections.emptyList());
        for (TrainConnection tc : nextConnections) {
            if (path.contains(tc)) continue; // prevent cycles by reusing same connection object

            // check layover constraints
            if (!path.isEmpty()) {
                // check layover time if this is not the first leg
                TrainConnection prev = path.get(path.size() - 1);

                // parse times with date offset to handle overnight layovers
                LocalDateTime prevArrival  = parseTimeWithOffset(prev.getArrivalTime());
                LocalDateTime nextDeparture = parseTimeWithOffset(tc.getDepartureTime());
                // check layover policy
                if (!isLayoverAcceptable(prevArrival, nextDeparture)) {
                    // skip this leg if layover is not acceptable
                    continue;
                }
            }

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

            double avgFirst = route.stream().mapToDouble(TrainConnection::getFirstClassRate).average().orElse(Double.NaN);
            double avgSecond = route.stream().mapToDouble(TrainConnection::getSecondClassRate).average().orElse(Double.NaN);
            System.out.printf("Average 1st Class Rate: %.2f, Average 2nd Class Rate: %.2f%n", avgFirst, avgSecond);
        }
    }


    /* ---------------------------------------------------------------------
     * Search and related helpers
     * ------------------------------------------------------------------*/
    // Performs a search for matching TrainConnection objects using current userDepartureCity/userArrivalCity and optional filters.
    public static List<TrainConnection> search() {
        DurationCalculator durationCalculator = new DurationCalculator();
        Map<String, String> appliedFilters = gatherFilters();
        List<TrainConnection> connectionsList = trainConnectionsDB.findConnections(userDepartureCity, userArrivalCity, appliedFilters);

        if (connectionsList != null && !connectionsList.isEmpty()) {
            for (TrainConnection tc : connectionsList) {
                double duration = durationCalculator.computeAllTripDurations(tc);
                tc.setDuration(duration); // store duration on the connection for sorting/display
            }
            return connectionsList;
        } else {
            // No direct connections found — try indirect routes (1-stop and 2-stops as implemented in DB)
            List<List<TrainConnection>> paths = trainConnectionsDB.findIndirectConnections(userDepartureCity, userArrivalCity);
            List<TrainConnection> indirectConnections = new ArrayList<>();

            for (List<TrainConnection> route : paths) {
                for (TrainConnection tc : route) {
                    double duration = durationCalculator.computeAllTripDurations(tc);
                    tc.setDuration(duration);
                    indirectConnections.add(tc);
                }
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
            if (!ascending) {
                Collections.reverse(routes);
            }
        } else if (sortParameter.equals("firstClassRate")) {
            routes.sort(Comparator.comparingDouble(SystemDriver::computeAverageFirstClassRate));
            if (!ascending) {
                Collections.reverse(routes);
            }
        } else if (sortParameter.equals("secondClassRate")) {
            routes.sort(Comparator.comparingDouble(SystemDriver::computeAverageSecondClassRate));
            if (!ascending) {
                Collections.reverse(routes);
            }
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
            if (i > 0) {
                total += calculateTransferTime(route.get(i - 1), route.get(i));
            }
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
            recordInput(userDepartureCity, userArrivalCity, option, value);
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
            System.out.println("5. Select your connection(s)");
            System.out.println("6. Exit");
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
                    // Book a trip
                    System.out.println("\nPlease select your desired trip from the displayed list above.");
                    try {
                        int userTripOption = Integer.parseInt(scanner.nextLine().trim());
                        bookTrip(userTripOption);
                        sortingMenu = false;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number. Returning to sorting menu.");
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

    //Validate a single input option/value pair. Returns true if the value is acceptable for the option.
    public static boolean validateInput(String option, String value) {
        switch (option) {
            case "depDay", "arrDay", "trainType": // departure day, arrival day, train type
                return value != null && !value.trim().isEmpty();
            case "depTime", "arrTime": // departure time, arrival time
                // Accepts 24-hour format
                return value.matches("([01]?\\d|2[0-3]):[0-5]\\d");
            case "minFirstClassPrice", "maxFirstClassPrice", "minSecondClassPrice", "maxSecondClassPrice": // min/max prices
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

    public static void recordInput(String departureCity, String arrivalCity, String option, String value) {
        System.out.println("Recorded input for route " + departureCity + " → " + arrivalCity + ": "
                + option + " = " + value);
    }

    /* ---------------------------------------------------------------------
     * Booking Section
     * ------------------------------------------------------------------*/
    //Login register so they can view their trip using last name and id
    public static boolean login() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your last name:");
        String lastName = scanner.nextLine().trim().toLowerCase();
        System.out.println("Enter your id:");
        String id = scanner.nextLine().trim().toLowerCase();

        Client client = clientDB.getClientByLastNameAndID(lastName, id);
        if (client != null) {
            System.out.println("Login successful. Welcome, " + client.getFirstName() + " " + client.getLastName() + "!");
            return true;
        } else {
            return false;
        }
    }

    public static void bookTrip(int userTripOption) {

    List<TrainConnection> trainConnections = search();
    List<TrainConnection> selectedRoutes = getRoutes(trainConnections, userTripOption, userDepartureCity, userArrivalCity);
    Trip trip = tripDB.createTrip(selectedRoutes);

    Scanner scanner = new Scanner(System.in);

    while (true) {
        // Collect client information
        System.out.println("Enter your first name: ");
        String firstName = scanner.nextLine().trim();

        System.out.println("Enter your last name: ");
        String lastName = scanner.nextLine().trim();

        System.out.println("Enter your age: ");
        int age = Integer.parseInt(scanner.nextLine().trim());

        Client c = clientDB.createClient(firstName, lastName, age);

        if (c == null) {
            System.out.println("Failed to register client. Please try again.");
            continue;
        }

        System.out.println("Client registered with ID: " + c.getClientId());

        // Ask user for ticket class
        System.out.println("Choose ticket class:");
        System.out.println("1. First Class");
        System.out.println("2. Second Class");
        String classChoice = scanner.nextLine().trim();
        
        // Calculate total cost based on selected routes and class
        double totalCost = 0.0;
        if (classChoice.equals("1")) {
            for (TrainConnection tc : selectedRoutes) {
                totalCost += tc.getFirstClassRate();
            }
        } else {
            for (TrainConnection tc : selectedRoutes) {
                totalCost += tc.getSecondClassRate();
            }
        }

        // Convert routes to string for storage
        String routesString = selectedRoutes.stream()
            .map(tc -> tc.getDepartureCity() + "->" + tc.getArrivalCity())
            .collect(Collectors.joining(","));

        // Create reservation in DATABASE first
        Reservation r = reservationDB.createReservation(c, trip.getTripId(), routesString);
        
        if (r == null) {
            System.out.println("Failed to create reservation. Please try again.");
            continue;
        }

        // Add to in-memory trip object
        tripDB.addReservationToTrip(trip, r);
        
        // Create ticket with calculated cost
        Ticket ticket = ticketDB.createTicket(totalCost);  // Pass the cost
        if (ticket != null) {
            r.setTicket(ticket);
            updateReservationWithTicket(r.getReservationID(), ticket.getTicketId());
            System.out.println("Ticket cost: $" + String.format("%.2f", totalCost));
        }

        System.out.println(trip.getSummary());
        System.out.println("Your tickets have been saved. Thank you for booking with us!\n");

        System.out.println("Add another traveller? (y/n)");
        String more = scanner.nextLine().trim().toLowerCase();
        if (!more.equals("y") && !more.equals("yes")) {
            break;
        }
    }

    System.out.println(trip.getSummary());
}

    private static void updateReservationWithTicket(String reservationID, String ticketID) {
        try (var conn = DriverManager.getConnection("jdbc:sqlite:my.db"); var pstmt = conn.prepareStatement("UPDATE Reservation SET ticketID = ? WHERE reservationID = ?")) {
            pstmt.setString(1, ticketID);
            pstmt.setString(2, reservationID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating reservation with ticket: " + e.getMessage());
        }
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
    private static void addDeparture(String city) {
        userDepartureCity = city;
    }

    private static void addArrival(String city) {
        userArrivalCity = city;
    }


    /*
      Lookup a client by last name and id, then print that client's trips (if any).
     */
    public static void viewTrips(String lastName, String clientID) {
        if (lastName == null) {
            lastName = "";
        }
        Client client = clientDB.getClientByLastNameAndID(lastName.toLowerCase(), clientID);
        if (client == null) {
            System.out.println("Client not found. Returning to main menu.");
            return;
        }

        //Client found = clientDB.getClients().get(clientIndex);
        System.out.println("Login successful. Welcome, " + client.getFirstName() + " " + client.getLastName() + "!");
        List<Trip> clientTrips = tripDB.getTripsForClient(client);

        if (clientTrips == null || clientTrips.isEmpty()) {
            System.out.println("No trips found for " + client.getFirstName() + " " + client.getLastName() + ".\n");
            return;
        }

        System.out.println("\n=== Trips for " + client.getFirstName() + " " + client.getLastName() + " ===\n");
        for (Trip t : clientTrips) {
            System.out.println(t.getSummary());
        }
    }

    /* ---------------------------------------------------------------------
     * Layover policy implementation
     * ------------------------------------------------------------------*/

    /**
     * Returns true when the time is considered "after hours".
     * After hours defined as 22:00 .. 05:59:59
     */
    private static boolean isAfterHours(LocalTime time) {
        // true if before DAY_START or at/after DAY_END
        return time.isBefore(DAY_START) || !time.isBefore(DAY_END);
    }

    /**
     * Compute layover Duration between arrival and next departure.
     * If nextDeparture is before arrival, this returns a negative duration.
     */
    private static Duration computeLayover(LocalDateTime arrival, LocalDateTime nextDeparture) {
        return Duration.between(arrival, nextDeparture);
    }

    /**
     * Policy check for whether a layover is acceptable.
     *
     * Policy:
     *  - Reject extremely long layovers longer than 48 hours.
     *  - If either arrival or next departure is in after hours (22:00-05:59) then
     *    require a short layover: between 5 and 30 minutes.
     *  - Otherwise (daytime both sides) require a layover between 60 and 120 minutes.
     */
    private static boolean isLayoverAcceptable(LocalDateTime arrival, LocalDateTime nextDeparture) {
        Duration layover = computeLayover(arrival, nextDeparture);

        if (layover.compareTo(Duration.ofHours(48)) > 0) {
            return false;
        }

        boolean arrivalAfterHours = isAfterHours(arrival.toLocalTime());
        boolean departureAfterHours = isAfterHours(nextDeparture.toLocalTime());

        if (arrivalAfterHours || departureAfterHours) {
            Duration min = Duration.ofMinutes(5);
            Duration max = Duration.ofMinutes(30);
            return !layover.minus(min).isNegative() && layover.compareTo(max) <= 0;
        } else {
            Duration min = Duration.ofMinutes(60);
            Duration max = Duration.ofMinutes(120);
            return !layover.minus(min).isNegative() && layover.compareTo(max) <= 0;
        }
    }

    // Parses a time string (e.g. "18:30", "03:11 (+1d)") and returns a LocalDateTime, using 2000-01-01 as the base date. If "(+1d)" is present, adds one day to the date.
    private static LocalDateTime parseTimeWithOffset(String timeStr) {
        boolean plusOneDay = timeStr.contains("(+1d)");

        // Extract HH:MM part
        String hhmm = timeStr.split(" ")[0];
        LocalTime time = LocalTime.parse(hhmm);

        LocalDate baseDate = LocalDate.of(2000, 1, 1);
        if (plusOneDay) {
            baseDate = baseDate.plusDays(1);
        }

        return LocalDateTime.of(baseDate, time);
    }



}
