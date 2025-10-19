package src;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Trip {
    private double tripDuration;
    private String tripId;
    private String status;
    private List<Reservation> reservations;
    private List<TrainConnection> routes;

    public Trip() {
        this.tripDuration = 0.0;
        this.tripId = generateTripId();
        this.reservations = new ArrayList<>();
    }

    public Trip(List<TrainConnection> routes) {
        this.tripDuration = 0.0;
        this.routes = routes;
        this.tripId = generateTripId();
        this.reservations = new ArrayList<>();
    }

    public double getTripDuration() {
        return tripDuration;
    }

    public void setTripDuration(double tripDuration) {
        this.tripDuration = tripDuration;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripID) {
        this.tripId = tripId;
    }

    private static String generateTripId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

        public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Reservation addReservation(Reservation r) {
        reservations.add(r);
        return r;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TrainConnection> getRoutes() {
        return routes;
    }

    public double computeTripDuration(TrainConnection tc) {
        String departureTime = tc.getDepartureTime();
        String arrivalTime = tc.getArrivalTime();

        arrivalTime = arrivalTime.replaceAll("\\s*\\(\\+\\d+d\\)", "");

        String[] depParts = departureTime.split(":");
        String[] arrParts = arrivalTime.split(":");

        int depHours = Integer.parseInt(depParts[0]);
        int depMinutes = Integer.parseInt(depParts[1]);
        int arrHours = Integer.parseInt(arrParts[0]);
        int arrMinutes = Integer.parseInt(arrParts[1]);

        if (arrHours < depHours || (arrHours == depHours && arrMinutes < depMinutes)) {
            arrHours += 24;
        }

        int totalDepMinutes = depHours * 60 + depMinutes;
        int totalArrMinutes = arrHours * 60 + arrMinutes;

        int durationInMinutes = totalArrMinutes - totalDepMinutes;

        this.tripDuration = durationInMinutes / 60.0;
        return this.tripDuration;
    }
    
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(String.format("Trip ID: TRP-%s\n", getTripId().substring(0, 6).toUpperCase()));

        // Connection line
        if (routes != null && !routes.isEmpty()) {
            sb.append("Connection: ");
            sb.append(safe(routes.get(0).getDepartureCity()));
            for (TrainConnection rc : routes) {
                sb.append(" \u2192 "); // Unicode arrow
                sb.append(safe(rc.getArrivalCity()));
            }
            sb.append("\n");

            // Departure and Arrival
            sb.append(String.format("Departure:  %s %s\n",
                    safe(routes.get(0).getDaysOfOperation() != null && !routes.get(0).getDaysOfOperation().isEmpty() ? routes.get(0).getDaysOfOperation().get(0) : "n/a"),
                    safe(routes.get(0).getDepartureTime())));
            sb.append(String.format("Arrival:    %s %s\n",
                    safe(routes.get(routes.size() - 1).getDaysOfOperation() != null && !routes.get(routes.size() - 1).getDaysOfOperation().isEmpty() ? routes.get(routes.size() - 1).getDaysOfOperation().get(0) : "n/a"),
                    safe(routes.get(routes.size() - 1).getArrivalTime())));

            // Stops
            if (routes.size() > 1) {
                sb.append("Stops: " + (routes.size() - 1) + " (");
                for (int i = 0; i < routes.size() - 1; i++) {
                    sb.append(safe(routes.get(i).getArrivalCity()));
                    if (i < routes.size() - 2) sb.append(", ");
                }
                sb.append(")\n");
            } else {
                sb.append("Stops: 0\n");
            }
        }

        sb.append("\nTravellers:\n");
        sb.append("________________________________________________\n");
        sb.append(String.format("%-16s %-4s %-8s %-14s\n", "Name", "Age", "ID", "Ticket Number"));
        sb.append("________________________________________________\n");

        if (reservations != null && !reservations.isEmpty()) {
            for (Reservation r : reservations) {
                Client c = r.getClient();
                String name = (c != null) ? (safe(c.getFirstName()) + " " + safe(c.getLastName())) : "Unnamed";
                String age = (c != null) ? String.valueOf(c.getAge()) : "n/a";
                String id = (c != null) ? safe(String.valueOf(c.getClientId())) : "n/a";
                String ticketId = (r.getTicket() != null)
                        ? String.format("TK-%03d", r.getTicket().getTicketId())
                        : "n/a";
                sb.append(String.format("%-16s %-4s %-8s %-14s\n", name, age, id, ticketId));            }
        } else {
            sb.append("No reservations\n");
        }
        sb.append("________________________________________________\n");

        sb.append("\nStatus: " + (status == null ? "CURRENT" : status.toUpperCase()) + "\n");
        sb.append("Your tickets have been saved. Thank you for booking with us!\n");

        return sb.toString();
    }


    // helper used inside getSummary
    private static String safe(String s) {
        return s == null ? "n/a" : s;
    }


}
