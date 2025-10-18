package src;

import java.util.ArrayList;
import java.util.List;

public class Trip {
    private double tripDuration;
    private String tripID;
    private List<Reservation> reservations;

    public Trip() {
        this.tripDuration = 0.0;
        this.reservations = new ArrayList<>();
    }

    public double getTripDuration() {
        return tripDuration;
    }

    public void setTripDuration(double tripDuration) {
        this.tripDuration = tripDuration;
    }

    public String getTripID() {
        return tripID;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
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

}
