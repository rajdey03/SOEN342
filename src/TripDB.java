package src;

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

}
