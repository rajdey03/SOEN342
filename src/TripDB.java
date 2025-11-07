package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TripDB {
    private List<Trip> trips;

    public TripDB() {
        trips = new ArrayList<Trip>();
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public Trip createTrip(List<TrainConnection> routes) {
        Trip t = new Trip(routes);
        t.setStatus("Current");
        trips.add(t);
        return t;
    }

    public void addReservationToTrip(Trip t, Reservation r) {
        t.addReservation(r);
    }

    
    public List<Trip> getTripsForClient(Client client) {
        List<Trip> clientTrips = new ArrayList<>();
        if (client == null) return clientTrips;

        for (Trip trip : trips) {
            for (Reservation r : trip.getReservations()) {
                if (r.getClient() != null && r.getClient().getClientId() == client.getClientId()) {
                    clientTrips.add(trip);
                    break; 
                }
            }
        }
        return clientTrips;
    }
       /** Save trips to a CSV file **/
    public void saveTripsToFile(String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("tripId,clientId,tripDuration,status");
            bw.newLine();
            for (Trip t : trips) {
                   bw.write(String.format("%s,%d,%d,%s",
                    csvEscape(t.getTripId()),
                    t.getClientId(),
                    t.getTripDuration(),
                    csvEscape(t.getStatus())
                   ));
            bw.newLine();
            }
        }
    }

    /** Load trips from a CSV file **/
    public void loadTripsFromFile(String path) throws IOException {
        trips.clear();
        File f = new File(path);
        if (!f.exists()) return; // no saved trips yet

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = parseCSV(line);
                if (parts.length >= 7) {
                    Trip t = new Trip();
                    t.setTripId((parts[0]));
                    t.setClientId((parts[1]));
                    t.setTripDuration(Double.parseDouble(parts[2]));
                    t.setStatus(parts[3]);
                    trips.add(t);
                }
            }
        }
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String[] parseCSV(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

}

